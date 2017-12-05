package network.pluto.absolute;

import io.undertow.UndertowOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.embedded.undertow.UndertowEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(JpaProperties.class)
@EnableCaching
@SpringBootApplication
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
