package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DashboardStepConfig {
    private final DashboardBatchSupport BatchSupport;
    protected final DashboardReaderConfig dashboardReaderConfig;
    protected final DashboardProcessorConfig dashboardProcessorConfig;
    protected final DashboardWriterConfig dashboardWriterConfig;

    @Bean
    public Step userStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("userStatsStep", BatchSupport.jobRepository)
                .<User, User>chunk(1000, BatchSupport.transactionManager)
                .reader(dashboardReaderConfig.userStatsReader(BatchSupport.entityManagerFactory))
                .writer(dashboardWriterConfig.userStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean
    public Step storeStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("storeStatsStep", BatchSupport.jobRepository)
                .<Store, Store>chunk(1000, BatchSupport.transactionManager)
                .reader(dashboardReaderConfig.storeStatsReader(BatchSupport.entityManagerFactory))
                .writer(dashboardWriterConfig.storeStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean
    public Step paymentStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("paymentStatsStep", BatchSupport.jobRepository)
                .<Payment, Payment>chunk(1000, BatchSupport.transactionManager)
                .reader(dashboardReaderConfig.paymentStatsReader())
                .writer(dashboardWriterConfig.paymentStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean
    public Step saveDashboardStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("saveDashboardStep", BatchSupport.jobRepository)
                .<DashboardStatsAccumulator, Dashboard>chunk(1, BatchSupport.transactionManager)
                .reader(dashboardReaderConfig.accumulatorReader(dashboardStatsAccumulator))
                .processor(dashboardProcessorConfig.dashboardStatsAccumulatorProcessor())
                .writer(dashboardWriterConfig.dashboardWriter())
                .build();
    }

    @Bean
    public Step updatePopularStoreStep() {
        return new StepBuilder("updatePopularStoreStep", BatchSupport.jobRepository)
                .<Store, PopularStore>chunk(10, BatchSupport.transactionManager)
                .reader(dashboardReaderConfig.popularStoreReader())
                .processor(dashboardProcessorConfig.popularStoreProcessor())
                .writer(dashboardWriterConfig.popularStoreWriter())
                .build();
    }

    @Bean
    public Step updateStoreSalesRankStep() {
        return new StepBuilder("updateStoreSalesRankStep", BatchSupport.jobRepository)
                .<Store, StoreSalesRank>chunk(1000, BatchSupport.transactionManager)
                .reader(dashboardReaderConfig.storeSalesRankReader())
                .processor(dashboardProcessorConfig.storeSalesRankProcessor())
                .writer(dashboardWriterConfig.storeSalesRankWriter())
                .build();
    }
}
