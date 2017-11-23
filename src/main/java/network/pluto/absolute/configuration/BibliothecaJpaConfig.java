package network.pluto.absolute.configuration;

import network.pluto.bibliotheca.models.BibliothecaModel;
import network.pluto.bibliotheca.repositories.BibliothecaRepository;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
@EnableJpaRepositories(basePackageClasses = { BibliothecaRepository.class })
@Configuration
public class BibliothecaJpaConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                       JpaProperties properties,
                                                                       @Named("dataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .properties(properties.getHibernateProperties(dataSource))
                .packages(Jsr310JpaConverters.class, BibliothecaModel.class)
                .persistenceUnit("bibliotheca")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager transactionManager(@Named("entityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }
}
