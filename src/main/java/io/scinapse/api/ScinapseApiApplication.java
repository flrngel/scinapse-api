package io.scinapse.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;
import java.time.ZoneOffset;
import java.util.TimeZone;

@EnableCaching
@EnableJpaRepositories
@EntityScan
@SpringBootApplication
public class ScinapseApiApplication {

    @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
    }

    public static void main(String[] args) {
        SpringApplication.run(ScinapseApiApplication.class, args);
    }

}
