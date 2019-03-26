package io.scinapse.domain.configuration;

import io.scinapse.domain.ScinapseDomain;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackageClasses = { ScinapseDomain.class })
@EntityScan(basePackageClasses = { ScinapseDomain.class, Jsr310JpaConverters.class })
@Configuration
public class ScinapseJpaConfig {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.jpa")
    public JpaProperties jpaProperties() {
        JpaProperties jpaProperties = new JpaProperties();
        jpaProperties.getProperties().put("hibernate.dialect", SQLServerDateDialect.class.getCanonicalName());
        return jpaProperties;
    }

}
