package org.lili.forfun.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication(scanBasePackages = {"org.lili.forfun.infra"})
@PropertySources({
    @PropertySource("classpath:test_config.properties")
})
@Slf4j
public class TestAnimallBaseApplication {
    public static void main(String[] args) {
        try {
            SpringApplication.run(TestAnimallBaseApplication.class, args);
            Thread.sleep(100000000);
        } catch (Throwable e) {
            log.error("",e);
        }
    }
}
