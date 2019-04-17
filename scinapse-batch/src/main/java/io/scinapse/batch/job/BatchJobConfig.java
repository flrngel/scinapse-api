package io.scinapse.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job collectionEmailJob() {
        return jobBuilderFactory.get("collection_email_job")
                .start(collectionEmailStep(null))
                .build();
    }

    @Bean
    public Step collectionEmailStep(CollectionEmailTasklet tasklet) {
        return stepBuilderFactory.get("collection_email_step")
                .tasklet(tasklet)
                .build();
    }

    @Bean
    public Job signUpD1EmailJob() {
        return jobBuilderFactory.get("sign_up_d1_email_job")
                .start(signUpD1EmailStep(null))
                .build();
    }

    @Bean
    public Step signUpD1EmailStep(SignUpD1EmailTasklet tasklet) {
        return stepBuilderFactory.get("sign_up_d1_email_step")
                .tasklet(tasklet)
                .build();
    }

    @Bean
    public Job signUpD3EmailJob() {
        return jobBuilderFactory.get("sign_up_d3_email_job")
                .start(signUpD3EmailStep(null))
                .build();
    }

    @Bean
    public Step signUpD3EmailStep(SignUpD3EmailTasklet tasklet) {
        return stepBuilderFactory.get("sign_up_d3_email_step")
                .tasklet(tasklet)
                .build();
    }

}
