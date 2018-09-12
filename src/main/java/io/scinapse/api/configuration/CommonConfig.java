package io.scinapse.api.configuration;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import javax.servlet.Filter;
import java.nio.charset.Charset;

@Configuration
public class CommonConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .additionalMessageConverters(new FormHttpMessageConverter())
                .additionalMessageConverters(new StringHttpMessageConverter(Charset.forName("UTF-8")))
                .setConnectTimeout(2000)
                .setReadTimeout(3000)
                .build();
    }

    @Bean
    public RestHighLevelClient restHighLevelClient(@Value("${pluto.server.es.hostname}") String hostname,
                                                   @Value("${pluto.server.es.port}") int port,
                                                   @Value("${pluto.server.es.scheme}") String scheme) {

        RestClientBuilder builder = RestClient
                .builder(new HttpHost(hostname, port, scheme))
                // we cannot use bug fixed client(6.3.1+) due to ES version conflict(6.2)
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectionRequestTimeout(0));

        return new RestHighLevelClient(builder);
    }

    @Bean
    public Filter xRayFilter(@Value("${pluto.server.name}") String serverName) {
        return new AWSXRayServletFilter(serverName);
    }

}
