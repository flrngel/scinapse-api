package io.scinapse.api.configuration;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import com.sendgrid.SendGrid;
import org.apache.http.HttpHost;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.config.Lookup;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.CookieSpecRegistries;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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
        CloseableHttpClient client = HttpClients.custom()
                .setDefaultCookieSpecRegistry(createCookieSpecRegistry())
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);

        return builder
                .requestFactory(factory)
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
                .setRequestConfigCallback(config -> config.setConnectionRequestTimeout(0))

                // prevent "Invalid 'expires' attribute" WARN log
                .setHttpClientConfigCallback(config -> config.setDefaultCookieSpecRegistry(createCookieSpecRegistry()));

        return new RestHighLevelClient(builder);
    }

    private Lookup<CookieSpecProvider> createCookieSpecRegistry() {
        String[] datePatterns = {
                "EEE, dd-MMM-yy HH:mm:ss z", // from NetscapeDraftSpec.EXPIRES_PATTERN
                DateUtils.PATTERN_RFC1123, // from RFC6265StrictSpec.DATE_PATTERNS
                DateUtils.PATTERN_RFC1036,
                DateUtils.PATTERN_ASCTIME
        };

        // default cookie spec does not have standard 'expires' date patterns
        // we need to override default date pattern
        CookieSpecProvider defaultProvider = new DefaultCookieSpecProvider(
                DefaultCookieSpecProvider.CompatibilityLevel.DEFAULT,
                PublicSuffixMatcherLoader.getDefault(),
                datePatterns,
                false);

        // only override default cookie spec
        return CookieSpecRegistries.createDefaultBuilder()
                .register(CookieSpecs.DEFAULT, defaultProvider)
                .build();
    }

    @Bean
    public SendGrid sendGrid(@Value("${pluto.server.email.sg.api-key}") String apiKey) {
        return new SendGrid(apiKey);
    }

    @Bean
    public Filter xRayFilter(@Value("${pluto.server.name}") String serverName) {
        return new AWSXRayServletFilter(serverName);
    }

}
