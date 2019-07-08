package org.lili.forfun.infra.domain.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class MqConfig {
    @Value("${mq.endpoints}")
    private String endpoints;
    @Value("${mq.username:animal}")
    private String username;
    @Value("${mq.password:animal}")
    private String password;
    @Value("${mq.port:5672}")
    private int port;
}

