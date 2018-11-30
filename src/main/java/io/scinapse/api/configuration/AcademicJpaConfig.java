package io.scinapse.api.configuration;

import io.scinapse.api.data.academic.Academic;
import io.scinapse.api.util.OffsetDateTimeConverter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@EnableJpaRepositories(
        basePackageClasses = { Academic.class },
        entityManagerFactoryRef = "academicEntityManagerFactory",
        transactionManagerRef = AcademicJpaConfig.ACADEMIC_TX_MANAGER)
@Configuration
public class AcademicJpaConfig {

    public static final String ACADEMIC_TX_MANAGER = "academicTransactionManager";

    @Bean
    @ConfigurationProperties("academic.datasource")
    public DataSource academicDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean academicEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                               @Named("academicDataSource") DataSource dataSource,
                                                                               JpaProperties properties) {
        return builder
                .dataSource(dataSource)
                .packages(Jsr310JpaConverters.class, OffsetDateTimeConverter.class, Academic.class)
                .persistenceUnit("academic")
                .properties(properties.getHibernateProperties(dataSource))
                .build();
    }

    @Bean
    public PlatformTransactionManager academicTransactionManager(@Named("academicEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

}
