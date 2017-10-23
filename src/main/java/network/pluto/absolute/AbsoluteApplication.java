package network.pluto.absolute;

import io.undertow.UndertowOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan({"network.pluto.absolute", "network.pluto.bibliotheca"})
@EnableJpaAuditing
@EnableJpaRepositories("network.pluto.bibliotheca.repositories")
@EntityScan(
        value = "network.pluto.bibliotheca.models",
        basePackageClasses = Jsr310JpaConverters.class)
public class AbsoluteApplication {

    public static void main(String[] args) {
        SpringApplication.run(AbsoluteApplication.class, args);
    }

    @Bean
    public UndertowEmbeddedServletContainerFactory undertowEmbeddedServletContainerFactory() {
        UndertowEmbeddedServletContainerFactory factory = new UndertowEmbeddedServletContainerFactory();
        factory.addBuilderCustomizers(builder -> builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true));
        return factory;
    }
}
