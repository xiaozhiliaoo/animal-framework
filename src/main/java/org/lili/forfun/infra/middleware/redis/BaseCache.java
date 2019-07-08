package org.lili.forfun.infra.middleware.redis;

import com.alibaba.fastjson.JSON;
import org.lili.forfun.infra.domain.config.EnvConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lili
 * @description
 * @create 2019/7/8 14:42
 */
public abstract class BaseCache {

    @Autowired
    protected RedisClient redisClient;
    @Autowired
    protected EnvConfig envConfig;

    protected <T> List<T> getAllFromSet(String key, Class<T> clz) {
        Set<String> set = redisClient.getAllFromSet(key);
        return set.stream().map(json -> JSON.parseObject(json, clz))
                .collect(Collectors.toList());
    }

    protected Long removeAllFromSet(String key) {
        Set<String> set = redisClient.getAllFromSet(key);
        if (set != null && !set.isEmpty()) {
            return redisClient.removeFromSet(key, set.toArray(new String[set.size()]));
        } else {
            return 0L;
        }
    }

    protected <T> T readCache(String key, Class<T> clz) {
        String s = redisClient.readCache(key);
        if (s == null || s.isEmpty()) {
            return null;
        } else {
            return JSON.parseObject(s, clz);
        }
    }

    public void init(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public String env() {
        return envConfig.getEnv();
    }
}

