package org.lili.forfun.infra.middleware.redis;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.lili.forfun.infra.domain.config.ServiceConfig;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ServiceCache extends BaseCache {

    private String KEY;

    @PostConstruct
    public void init() {
        KEY = RedisKey.getServiceConfigKey(envConfig.getEnv());
    }

    /**
     * 创建服务配置
     *
     * @param serviceConfig
     * @return
     */
    public Long saveServiceConfig(ServiceConfig serviceConfig) {
        ServiceConfig serviceConfig0 = this.getServiceConfig(serviceConfig.getServiceKey());
        if (serviceConfig0 != null) {
            serviceConfig.setGroupKeys(serviceConfig0.getGroupKeys());
            this.removeServiceConfig(serviceConfig0);
        }
        return redisClient.addToSet(KEY, JSON.toJSONString(serviceConfig));
    }

    /**
     * 更新分组到服务
     *
     * @param serviceKey
     * @param groupKey
     * @return
     */
    public boolean saveGroupIntoService(String serviceKey, String groupKey) {
        ServiceConfig cfg = this.getServiceConfig(serviceKey);
        if (cfg == null) {
            return false;
        } else {
            if (cfg.getGroupKeys() == null) {
                cfg.setGroupKeys(Lists.newArrayList(groupKey));
                removeServiceConfig(cfg);
                redisClient.addToSet(KEY, JSON.toJSONString(cfg));
            } else if (!cfg.getGroupKeys().contains(groupKey)) {
                cfg.getGroupKeys().add(groupKey);
                removeServiceConfig(cfg);
                redisClient.addToSet(KEY, JSON.toJSONString(cfg));
            } else {
                //DO NOTHING
                return true;
            }
            return true;
        }
    }

    /**
     * 查询所有服务信息
     *
     * @return
     */
    public List<ServiceConfig> getAllServiceConfig() {
        return this.getAllFromSet(KEY, ServiceConfig.class);
    }

    /**
     * 查询指定名字的服务信息，如果没有则返回空
     *
     * @param serviceKey
     * @return
     */
    public ServiceConfig getServiceConfig(String serviceKey) {
        List<ServiceConfig> serviceConfigs = this.getAllServiceConfig();
        if (serviceConfigs != null) {
            List<ServiceConfig> l = serviceConfigs.stream()
                    .filter(o -> o.getServiceKey().equals(serviceKey))
                    .collect(Collectors.toList());
            if (l.size() == 0) {
                return null;
            } else if (l.size() > 1) {
                removeServiceConfig(l.get(1));
            }
            return l.get(0);
        } else {
            return null;
        }
    }

    /**
     * 删除某服务的配置信息
     *
     * @param cfg
     * @return
     */
    public Long removeServiceConfig(ServiceConfig cfg) {
        return redisClient.removeFromSet(KEY, JSON.toJSONString(cfg));
    }

}
