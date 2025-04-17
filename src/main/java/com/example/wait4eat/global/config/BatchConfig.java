package com.example.wait4eat.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job deshboardUpdateJob() {
        return new JobBuilder("dashboardUpdateJob", jobRepository)
                .start(updateDashboardStep())
                .build();
    }

    @Bean
    public Step updateDashboardStep() {
        return new StepBuilder("updateDashboardStep", jobRepository)
                .tasklet(dashboardUpdateTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet dashboardUpdateTasklet() {
        return (contribution, chunkContext) -> {
            return RepeatStatus.FINISHED;
        };
    }
}
