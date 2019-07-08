package org.lili.forfun.infra.middleware.redis;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.lili.forfun.infra.domain.config.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lili
 * @description
 * @create 2019/7/8 14:40
 */
@Service
@Slf4j
public class RedisClient {
    private static final int MAX_TOTAL = 100;
    private static final int MAX_WAIT_MILLIS = 1000;
    private static final int MAX_IDLE = 10;
    private Pool<Jedis> jedisPool;
    @Autowired
    @Getter
    @Setter
    private RedisConfig redisConfig;
    @Getter
    @Setter
    private int dbIndex = 0;

    @PostConstruct
    public void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxWaitMillis(MAX_WAIT_MILLIS);
        config.setMaxIdle(MAX_IDLE);
        if (redisConfig.getRedisPort() == 6379) {
            jedisPool = new JedisPool(config,
                    redisConfig.getRedisHost(),
                    redisConfig.getRedisPort(),
                    redisConfig.getTimeout(),
                    redisConfig.getRedisPwd());
            log.info("start jedisPool: host={}, port={}", redisConfig.getRedisHost(), redisConfig.getRedisPort());
        } else {
            Set<String> sentinels = new HashSet<String>();
            for (String host : redisConfig.getRedisHost().split(",")) {
                // 这里为了支持单机启动多个sentinel服务
                if (host.indexOf(":") == -1) {
                    sentinels.add(host + ":" + redisConfig.getRedisPort());
                } else {
                    sentinels.add(host);
                }
            }
            jedisPool = new JedisSentinelPool("my-redis", sentinels, redisConfig.getRedisPwd());
            log.info("start JedisSentinelPool: sentinels={}", sentinels);
        }
    }

    @PreDestroy
    public void destroy() {
        jedisPool.destroy();
    }

    // -- begin for cache --

    public String storeCache(String key, int expireTime, String json) {
        log.info("storeCache key={},json={}", key, json);
        if (json == null || json.isEmpty()) {
            return json;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.setex(key, expireTime, json);
        }
    }

    public String storeCache(String key, String json) {
        log.info("storeCache key={},json={}", key, json);
        if (json == null || json.isEmpty()) {
            return json;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.set(key, json);
        }
    }

    public String readCache(String key) {
        if (key == null) {
            return null;
        }
        log.debug("readCache key={}", key);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.get(key);
        }
    }

    public long[] removeCache(String... keys) {
        if (keys == null) {
            return new long[]{-1L};
        } else {
            long[] rs = new long[keys.length];

            log.warn("forceRemoveKey(key={}}", keys);
            try (Jedis jedis = jedisPool.getResource()) {
                int i = 0;
                for (String key : keys) {
                    jedis.select(getDbIndex());
                    rs[i++] = jedis.del(key);
                }
            }
            return rs;
        }
    }

    // -- end of cache --

    // -- begin for queue --

    public long pushToQueue(String key, String taskJson) {
        log.info("pushToQueue key={},taskJson={}", key, taskJson);
        if (taskJson == null || taskJson.isEmpty()) {
            return -1L;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            long result = jedis.lpush(key, taskJson);
            log.info("pushToQueue result={}", result);
            return result;
        }
    }

    public Long pushToQueue(String key, int expireTime, String taskJson) {
        log.info("pushToQueue key={},taskJson={}", key, taskJson);
        if (taskJson == null || taskJson.isEmpty()) {
            return -1L;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            Long result = jedis.lpush(key, taskJson);
            jedis.expire(key, expireTime);
            log.info("pushToQueue result={}", result);
            return result;
        }
    }

    public String popFromQueue(String key) {
        if (key == null) {
            return null;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            String json = jedis.rpop(key);
            if (json != null) {
                log.info("popFromQueue key={}, task={}", key, json);
            }
            return json;
        }
    }

    public List<String> getQueue(String key, long start, long end) {
        if (key == null) {
            return null;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.lrange(key, start, end);
        }
    }

    public List<String> getQueue(String key, long end) {
        return getQueue(key, 0, end);
    }

    public List<String> getQueue(String key) {
        if (key == null) {
            return null;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.lrange(key, 0, jedis.llen(key));
        }
    }

    public String getQueueByIndex(String key, long index) {
        if (key == null) {
            return null;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.lindex(key, index);
        }
    }

    public long getQueueLen(String key) {
        if (key == null) {
            return -1L;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.llen(key);
        }
    }

    // -- end of queue --

    // -- begin for set --

    public long addToSet(String key, String... json) {
        log.info("addToSet key={},json={}", key, json);
        if (json == null || json.length == 0) {
            return -1L;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            /** jedis.expire(key,3600); **/
            return jedis.sadd(key, json);
        }
    }

    public long removeFromSet(String key, String... json) {
        if (key == null) {
            return -1L;
        }
        log.info("removeFromSet key={},json={}", key, json);
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.srem(key, json);
        }
    }

    public Set<String> getAllFromSet(String key) {
        if (key == null) {
            return null;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            Set<String> smembers = jedis.smembers(key);
            if (smembers != null && !smembers.isEmpty()) {
                log.debug("getAllFromSet key={},smembers={}", key, smembers);
            }
            return smembers;
        }
    }

    public Long delKey(String key) {
        if (StringUtils.isEmpty(key)) {
            return -1L;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.del(key);
        }
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public String flushDB() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.flushDB();
        }
    }

    public String getMapValue(String key, String field) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            String result = jedis.hget(key, field);
            if (result != null && !result.isEmpty()) {
                log.debug("getMapValue key={},result={}", key, result);
            }
            return result;
        }
    }

    public Long setMapValue(String key, String field, String value) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.hset(key, field, value);
        }
    }

    public String setMap(String key, final Map<String, String> hash) {
        return setMap(key, hash, null);
    }

    public String setMap(String key, final Map<String, String> hash, Integer expireTime) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            String result = jedis.hmset(key, hash);
            if (expireTime != null) {
                jedis.expire(key, expireTime);
            }
            if (result != null && !result.isEmpty()) {
                log.debug("setMap key={},result={}", key, result);
            }
            return result;
        }
    }

    public long incrAndGet(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key).longValue();
        }
    }

    public String renameKey(String key, String newkey) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(newkey)) {
            return null;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(getDbIndex());
            return jedis.rename(key, newkey);
        }
    }

    public void setJedisPool(Pool<Jedis> jedisPool) {
        this.jedisPool = jedisPool;
    }
}
