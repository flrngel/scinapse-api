package io.scinapse.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.sendgrid.SendGrid;
import io.scinapse.domain.ScinapseDomain;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@EnableBatchProcessing
@SpringBootApplication(scanBasePackageClasses = { ScinapseDomain.class, ScinapseBatch.class })
public class ScinapseBatchApplication {

    public static void main(String[] args) {
        setup();
        SpringApplication.run(ScinapseBatchApplication.class, args);
    }

    private static void setup() {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Bean
    public SendGrid sendGrid(@Value("${pluto.server.email.sg.api-key}") String apiKey) {
        return new SendGrid(apiKey);
    }

}
