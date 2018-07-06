package io.scinapse.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableCaching
@EnableJpaAuditing
@EnableJpaRepositories
@EntityScan(basePackageClasses = { ScinapseApi.class, Jsr310JpaConverters.class })
@SpringBootApplication
public class ScinapseApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScinapseApiApplication.class, args);
    }

}
