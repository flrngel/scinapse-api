package io.scinapse.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
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

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }

    public static void main(String[] args) {
        SpringApplication.run(ScinapseApiApplication.class, args);
    }

}
