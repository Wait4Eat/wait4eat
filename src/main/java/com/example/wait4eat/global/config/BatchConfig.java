package com.example.wait4eat.global.config;

import com.example.wait4eat.domain.dashboard.DashboardRepository;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.repository.UserRepository;
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

import java.time.LocalDate;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PaymentRepository paymentRepository;
    private final DashboardRepository dashboardRepository;

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
            LocalDate yesterday = LocalDate.now().minusDays(1);

            Long totalUserCount = userRepository.count();
            Long dailyUserCount = userRepository.countByLoginDate(yesterday);
            Long totalStoreCount = storeRepository.count();
            Long dailyNewStoreCount = storeRepository.countByCreatedAt(yesterday);
            Long dailyTotalSales = paymentRepository.sumSalesByDate(yesterday);

            Dashboard dashboard = Dashboard.builder()
                    .totalUserCount(totalUserCount)
                    .dailyUserCount(dailyUserCount)
                    .totalStoreCount(totalStoreCount)
                    .dailyNewStoreCount(dailyNewStoreCount)
                    .dailyTotalSales(dailyTotalSales)
                    .statisticsDate(yesterday)
                    .build();

            dashboardRepository.save(dashboard);
            return RepeatStatus.FINISHED;
        };
    }
}
