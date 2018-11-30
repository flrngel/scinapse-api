package io.scinapse.api.configuration;

import io.scinapse.api.data.scinapse.Scinapse;
import io.scinapse.api.util.OffsetDateTimeConverter;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@EnableJpaRepositories(basePackageClasses = { Scinapse.class })
@Configuration
public class ScinapseJpaConfig {

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                       @Named("dataSource") DataSource dataSource,
                                                                       JpaProperties properties) {
        return builder
                .dataSource(dataSource)
                .packages(Jsr310JpaConverters.class, OffsetDateTimeConverter.class, Scinapse.class)
                .persistenceUnit("scinapse")
                .properties(properties.getHibernateProperties(dataSource))
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(@Named("entityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

}
