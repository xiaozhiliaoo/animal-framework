package org.lili.forfun.infra.domain.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
public class EtcdConfig {
    @Value("${nls.etcd.endpoints}")
    private String endpoints;
}
