package org.lili.forfun.infra.domain.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class DiscoveryConfig {
    @Value("${discovery.register.enabled:false}")
    private boolean registerEnable;
    @Value("${discovery.discovery.enabled:false}")
    private boolean discoveryEnable;
    @Value("${discovery.config.enabled:false}")
    private boolean configEnable;
    @Autowired
    private MqConfig mqConfig;
}