package org.lili.forfun.infra.domain.config.condition;

import org.lili.forfun.infra.api.ServiceConfigApi;
import org.lili.forfun.infra.api.ServiceDiscoveryApi;
import org.lili.forfun.infra.api.ServiceRegisterApi;
import org.lili.forfun.infra.domain.config.DiscoveryConfig;
import org.lili.forfun.infra.domain.config.EtcdConfig;
import org.lili.forfun.infra.domain.http.HttpProcessor;
import org.lili.forfun.infra.engine.config.ConfigEngine;
import org.lili.forfun.infra.middleware.redis.ServiceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DiscoveryJavaConfig {
    @Autowired
    private DiscoveryConfig discoveryConfig;
    @Autowired
    private HttpProcessor httpProcessor;
    @Autowired
    private ServiceCache serviceCache;
    @Autowired
    private EtcdConfig etcdConfig;

    @Bean
    public ConfigEngine configEngine() {
        return new ConfigEngine(serviceCache);
    }

    @Bean(name = "serviceConfigApi")
    @Conditional(ServiceConfigCondition.class)
    public ServiceConfigApi serviceConfigApi() {
        return new ServiceConfigApi(configEngine(), discoveryConfig);
    }

    @Bean(name = "serviceDiscoveryApi")
    @Conditional(ServiceDiscoveryCondition.class)
    public ServiceDiscoveryApi serviceDiscoveryApi() {
        return new ServiceDiscoveryApi(configEngine(), httpProcessor, etcdConfig);
    }

    @Bean(value = "serviceRegisterApi", initMethod = "init", destroyMethod = "destroy")
    @Conditional({ServiceRegisterCondition.class})
    public ServiceRegisterApi serviceRegisterApi() {
        return new ServiceRegisterApi(etcdConfig);
    }
}
