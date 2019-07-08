package org.lili.forfun.infra.domain.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class RedisConfig {
    @Value("${nls.redis.host}")
    private String redisHost;
    @Value("${nls.redis.port}")
    private Integer redisPort;
    @Value("${nls.redis.pw}")
    private String redisPwd;
    @Value("${nls.redis.timeout:10000}")
    private int timeout;
}