package network.pluto.absolute.configuration;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import com.amazonaws.xray.strategy.DynamicSegmentNamingStrategy;
import io.sentry.spring.SentryServletContextInitializer;
import network.pluto.absolute.error.SentryExceptionResolver;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.Filter;

@Configuration
public class CommonConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        builder.additionalMessageConverters(new FormHttpMessageConverter());
        return builder.build();
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(@Value("${pluto.server.es.hostname}") String hostname,
                                                   @Value("${pluto.server.es.port}") int port,
                                                   @Value("${pluto.server.es.scheme}") String scheme) {
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost(hostname, port, scheme)));
    }

    @Bean
    public HandlerExceptionResolver sentryExceptionResolver() {
        return new SentryExceptionResolver();
    }

    @Bean
    public ServletContextInitializer sentryServletContextInitializer() {
        return new SentryServletContextInitializer();
    }

    @Bean
    public Filter xRayFilter() {
        return new AWSXRayServletFilter(new DynamicSegmentNamingStrategy("absolute", "*.pluto.network"));
    }

}
