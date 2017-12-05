package network.pluto.absolute.configuration;

import network.pluto.bibliotheca.academic.Academic;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@EnableJpaAuditing
@EnableJpaRepositories(
        basePackageClasses = { Academic.class },
        entityManagerFactoryRef = "academicEntityManagerFactory",
        transactionManagerRef = "academicTransactionManager")
@Configuration
public class AcademicJpaConfig {

    @Bean
    @ConfigurationProperties("academic.datasource")
    public DataSource academicDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean academicEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                               JpaProperties properties,
                                                                               @Named("academicDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .properties(properties.getHibernateProperties(dataSource))
                .packages(Jsr310JpaConverters.class, Academic.class)
                .persistenceUnit("academic")
                .build();
    }

    @Bean
    public PlatformTransactionManager academicTransactionManager(@Named("academicEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
}
