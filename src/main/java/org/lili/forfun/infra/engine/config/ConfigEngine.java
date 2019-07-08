package org.lili.forfun.infra.engine.config;


import org.lili.forfun.infra.domain.config.ServiceConfig;
import org.lili.forfun.infra.middleware.redis.ServiceCache;

public class ConfigEngine {

    private final ServiceCache serviceCache;


    public ConfigEngine(ServiceCache serviceCache) {
        this.serviceCache = serviceCache;
    }

    public void createServiceConfig(ServiceConfig serviceConfig) {
        serviceCache.saveServiceConfig(serviceConfig);
    }

    public void updateServiceConfig(ServiceConfig serviceConfig) {
    }

    public void removeServiceConfig(String serviceKey) {

    }

    public ServiceConfig getServiceConfig(String serviceKey) {
        return null;
    }

}
