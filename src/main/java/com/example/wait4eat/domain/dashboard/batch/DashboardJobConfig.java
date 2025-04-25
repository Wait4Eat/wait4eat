package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.dto.DashboardStatsAccumulator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DashboardJobConfig {
    private final DashboardBatchSupport batchSupport;
    protected final DashboardStepConfig dashboardStepConfig;

    @Bean
    public Job dailyStatisticsJob(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new JobBuilder("dailyStatisticsJob", batchSupport.jobRepository)
                .start(dashboardStepConfig.userStatsStep(dashboardStatsAccumulator))
                .next(dashboardStepConfig.storeStatsStep(dashboardStatsAccumulator))
                .next(dashboardStepConfig.paymentStatsStep(dashboardStatsAccumulator))
                .next(dashboardStepConfig.saveDashboardStep(dashboardStatsAccumulator))
                .next(dashboardStepConfig.updatePopularStoreStep())
                .next(dashboardStepConfig.updateStoreSalesRankStep())
                .build();
    }
}
