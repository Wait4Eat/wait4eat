package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.dto.DashboardStatsAccumulator;
import com.example.wait4eat.domain.dashboard.dto.StoreWaitingStats;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DashboardStepConfig {
    private final DashboardBatchSupport batchSupport;
    private final DashboardReaderConfig dashboardReaderConfig;
    private final DashboardProcessorConfig dashboardProcessorConfig;
    private final DashboardWriterConfig dashboardWriterConfig;
    private final PopularStoreWriter popularStoreWriter;

    @Bean
    public Step userStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("userStatsStep", batchSupport.jobRepository)
                .<User, User>chunk(1000, batchSupport.transactionManager)
                .reader(dashboardReaderConfig.userStatsReader(batchSupport.entityManagerFactory))
                .writer(dashboardWriterConfig.userStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean
    public Step storeStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("storeStatsStep", batchSupport.jobRepository)
                .<Store, Store>chunk(1000, batchSupport.transactionManager)
                .reader(dashboardReaderConfig.storeStatsReader(batchSupport.entityManagerFactory))
                .writer(dashboardWriterConfig.storeStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean
    public Step paymentStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("paymentStatsStep", batchSupport.jobRepository)
                .<Payment, Payment>chunk(1000, batchSupport.transactionManager)
                .reader(dashboardReaderConfig.paymentStatsReader())
                .writer(dashboardWriterConfig.paymentStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean
    public Step saveDashboardStep(DashboardStatsAccumulator accumulator) {
        return new StepBuilder("saveDashboardStep", batchSupport.jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Dashboard dashboard = Dashboard.builder()
                            .totalUserCount(accumulator.getTotalUserCount())
                            .dailyUserCount(accumulator.getDailyUserCount())
                            .totalStoreCount(accumulator.getTotalStoreCount())
                            .dailyNewStoreCount(accumulator.getDailyNewStoreCount())
                            .dailyTotalSales(accumulator.getTotalDailySales())
                            .statisticsDate(batchSupport.getYesterday())
                            .build();

                    batchSupport.dashboardRepository.save(dashboard);
                    return RepeatStatus.FINISHED;
                }, batchSupport.transactionManager)
                .build();
    }

    @Bean
    public Step updatePopularStoreStep() {
        return new StepBuilder("updatePopularStoreStep", batchSupport.jobRepository)
                .<Store, StoreWaitingStats>chunk(1000, batchSupport.transactionManager)
                .reader(dashboardReaderConfig.storeReader())
                .processor(dashboardProcessorConfig.popularStoreProcessor())
                .writer(popularStoreWriter)
                .build();
    }

    @Bean
    public Step updateStoreSalesRankStep() {
        return new StepBuilder("updateStoreSalesRankStep", batchSupport.jobRepository)
                .<Store, StoreSalesRank>chunk(1000, batchSupport.transactionManager)
                .reader(dashboardReaderConfig.storeReader())
                .processor(dashboardProcessorConfig.storeSalesRankProcessor())
                .writer(dashboardWriterConfig.storeSalesRankWriter())
                .build();
    }
}
