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
public class TestNlsBaseApplication {
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        try {
            SpringApplication.run(TestNlsBaseApplication.class, args);
            Thread.sleep(100000000);
        } catch (Throwable e) {
            log.error("",e);
        }
    }
}
