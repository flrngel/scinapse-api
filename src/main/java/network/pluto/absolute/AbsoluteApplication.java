package network.pluto.absolute;

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
@EntityScan(basePackageClasses = { Absolute.class, Jsr310JpaConverters.class })
@SpringBootApplication
public class AbsoluteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbsoluteApplication.class, args);
    }

}
