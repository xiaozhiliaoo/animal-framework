package org.lili.forfun.infra.domain.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Data
@Slf4j
public class FileConfig {
    @Value("${fs.endpoint}")
    private String endpoint;
    @Value("${fs.bucket}")
    private String bucket;
    @Value("${fs.accessid}")
    private String accessid;
    @Value("${fs.accesskey}")
    private String accesskey;
    @Value("${fs.replacelocal:false}")
    private boolean replacelocal;

    public FileConfig() {
        log.info("FileConfig.hashCode=" + this.hashCode());
    }
    
    @PostConstruct
    public void postConstruct() {
        log.info("FileConfig={}", this);
    }
}
