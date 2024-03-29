package org.lili.forfun.infra.domain.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class HttpConfig {
    @Value("${discovery.http.connectTimeout:5000}")
    private int connectTimeout;
    @Value("${discovery.http.writeTimeout:10000}")
    private int writeTimeout;
    @Value("${discovery.http.readTimeout:30000}")
    private int readTimeout;
}