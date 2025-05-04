package com.example.wait4eat.domain.dashboard.batch.step;

import com.example.wait4eat.domain.dashboard.batch.DashboardBatchSupport;
import com.example.wait4eat.domain.dashboard.batch.processor.DashboardProcessorConfig;
import com.example.wait4eat.domain.dashboard.batch.reader.DashboardReaderConfig;
import com.example.wait4eat.domain.dashboard.batch.writer.*;
import com.example.wait4eat.domain.dashboard.dto.StoreWaitingStats;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DashboardStepConfig {
    private final DashboardBatchSupport batchSupport;
    private final DashboardReaderConfig readerConfig;
    private final DashboardProcessorConfig processorConfig;
    private final UserStatsWriter userStatsWriter;
    private final StoreStatsWriter storeStatsWriter;
    private final PaymentStatsWriter paymentStatsWriter;
    private final PopularStoreWriter popularStoreWriter;
    private final StoreSalesStatsWriter storeSalesStatsWriter;
    private final StoreSalesRankWriter storeSalesRankWriter;

    @Bean
    public Step userStatsStep() {
        return new StepBuilder("userStatsStep", batchSupport.getJobRepository())
                .<User, User>chunk(1000, batchSupport.getTransactionManager())
                .reader(readerConfig.userStatsReader())
                .writer(userStatsWriter)
                .listener(batchSupport.executionContextPromotionListener())
                .build();
    }

    @Bean
    public Step storeStatsStep() {
        return new StepBuilder("storeStatsStep", batchSupport.getJobRepository())
                .<Store, Store>chunk(1000, batchSupport.getTransactionManager())
                .reader(readerConfig.storeReader())
                .writer(storeStatsWriter)
                .listener(batchSupport.executionContextPromotionListener())
                .build();
    }

    @Bean
    public Step paymentStatsStep() {
        return new StepBuilder("paymentStatsStep", batchSupport.getJobRepository())
                .<Payment, Payment>chunk(1000, batchSupport.getTransactionManager())
                .reader(readerConfig.paymentStatsReader())
                .writer(paymentStatsWriter)
                .listener(batchSupport.executionContextPromotionListener())
                .build();
    }

    @Bean
    public Step saveDashboardStep() {
        return new StepBuilder("saveDashboardStep", batchSupport.getJobRepository())
                .tasklet((contribution, chunkContext) -> {
                    JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
                    ExecutionContext context = jobExecution.getExecutionContext();

                    Long totalUserCount = context.getLong("totalUserCount");
                    Long dailyUserCount = context.getLong("dailyUserCount");
                    Long totalStoreCount = context.getLong("totalStoreCount");
                    Long dailyNewStoreCount = context.getLong("dailyNewStoreCount");
                    BigDecimal dailyTotalSales = (BigDecimal) context.get("dailyTotalSales");

                    Dashboard dashboard = Dashboard.builder()
                            .totalUserCount(totalUserCount)
                            .dailyUserCount(dailyUserCount)
                            .totalStoreCount(totalStoreCount)
                            .dailyNewStoreCount(dailyNewStoreCount)
                            .dailyTotalSales(dailyTotalSales)
                            .statisticsDate(batchSupport.getYesterday())
                            .build();

                    batchSupport.getDashboardRepository().save(dashboard);
                    return RepeatStatus.FINISHED;
                }, batchSupport.getTransactionManager())
                .build();
    }

    @Bean
    public Step updatePopularStoreStep() {
        return new StepBuilder("updatePopularStoreStep", batchSupport.getJobRepository())
                .<Store, StoreWaitingStats>chunk(1000, batchSupport.getTransactionManager())
                .reader(readerConfig.storeReader())
                .processor(processorConfig.popularStoreProcessor())
                .writer(popularStoreWriter)
                .build();
    }

    @Bean
    public Step storeSalesStatsStep() {
        return new StepBuilder("storeSalesStatsStep", batchSupport.getJobRepository())
                .<Store, StoreSalesRank>chunk(1000, batchSupport.getTransactionManager())
                .reader(readerConfig.storeReader())
                .processor(processorConfig.storeSalesRankProcessor())
                .writer(storeSalesStatsWriter)
                .build();
    }

    @Bean
    public Step updateStoreSalesRankingStep() {
        return new StepBuilder("updateStoreSalesRankingStep", batchSupport.getJobRepository())
                .<StoreSalesRank, StoreSalesRank>chunk(1000, batchSupport.getTransactionManager())
                .reader(readerConfig.storeSalesRankReader())
                .writer(storeSalesRankWriter)
                .build();
    }
}
