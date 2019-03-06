package io.scinapse.batch;

import com.sendgrid.SendGrid;
import io.scinapse.domain.ScinapseDomain;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication(scanBasePackageClasses = { ScinapseDomain.class, ScinapseBatch.class })
public class ScinapseBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScinapseBatchApplication.class, args);
    }

    @Bean
    public SendGrid sendGrid(@Value("${pluto.server.email.sg.api-key}") String apiKey) {
        return new SendGrid(apiKey);
    }

}
