package com.example.wait4eat.domain.dashboard.batch.job;

import com.example.wait4eat.domain.dashboard.batch.DashboardBatchSupport;
import com.example.wait4eat.domain.dashboard.batch.step.DashboardStepConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DashboardJobConfig {
    private final DashboardBatchSupport batchSupport;
    private final DashboardStepConfig dashboardStepConfig;

    @Bean
    public Job dailyStatisticsJob() {
        return new JobBuilder("dailyStatisticsJob", batchSupport.getJobRepository())
                .start(dashboardStepConfig.userStatsStep())
                .next(dashboardStepConfig.storeStatsStep())
                .next(dashboardStepConfig.paymentStatsStep())
                .next(dashboardStepConfig.saveDashboardStep())
                .next(dashboardStepConfig.updatePopularStoreStep())
                .next(dashboardStepConfig.storeSalesStatsStep())
                .next(dashboardStepConfig.updateStoreSalesRankingStep())
                .build();
    }
}
