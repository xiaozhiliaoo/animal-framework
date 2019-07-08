package org.lili.forfun.infra.domain.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class EnvConfig {
    @Value("${deploy.env}")
    private String env;
}
