package io.scinapse.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CollectionEmailJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job collectionEmailJob() {
        return jobBuilderFactory.get("collectionEmailJob")
                .incrementer(new RunIdIncrementer())
                .start(collectionEmailStep(null))
                .build();
    }

    @Bean
    public Step collectionEmailStep(CollectionEmailTasklet tasklet) {
        return stepBuilderFactory.get("collectionEmailStep")
                .tasklet(tasklet)
                .build();
    }

}