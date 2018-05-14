package network.pluto.absolute;

import network.pluto.bibliotheca.Bibliotheca;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableCaching
@EnableJpaAuditing
@EnableJpaRepositories(basePackageClasses = { Bibliotheca.class })
@EntityScan(basePackageClasses = { Jsr310JpaConverters.class, Bibliotheca.class })
@SpringBootApplication
public class AbsoluteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbsoluteApplication.class, args);
    }

}
