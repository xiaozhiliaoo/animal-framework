package org.lili.forfun.infra.api;


import lombok.extern.slf4j.Slf4j;
import org.lili.forfun.infra.domain.config.DiscoveryConfig;
import org.lili.forfun.infra.domain.config.ServiceConfig;
import org.lili.forfun.infra.engine.config.ConfigEngine;

/**
 * 服务配置
 */
@Slf4j
public class ServiceConfigApi {

    private final DiscoveryConfig config;
    private final ConfigEngine configEngine;

    public ServiceConfigApi(ConfigEngine configEngine, DiscoveryConfig config) {
        this.configEngine = configEngine;
        this.config = config;
    }

    public void createServiceConfig(ServiceConfig config) throws Exception {
        configEngine.createServiceConfig(config);
    }

    public void updateAppConfig(ServiceConfig config) throws Exception {

    }
    public void removeAppConfig(String appKey) throws Exception {

    }

}
