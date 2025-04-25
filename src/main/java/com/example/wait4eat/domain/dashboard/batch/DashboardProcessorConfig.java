package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.dto.StoreWaitingStats;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.store.entity.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

@Configuration
@RequiredArgsConstructor
public class DashboardProcessorConfig {
    private final DashboardBatchSupport batchSupport;

    @Bean
    @StepScope
    public ItemProcessor<Store, StoreWaitingStats> popularStoreProcessor() {
        return store -> {
            long waitingCount = batchSupport.waitingRepository
                    .countByStoreAndCreatedAtBetween(store, batchSupport.getStartDate(), batchSupport.getEndDate());
            return StoreWaitingStats.builder()
                    .store(store)
                    .waitingCount(waitingCount)
                    .build();
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<Store, StoreSalesRank> storeSalesRankProcessor() {
        return new ItemProcessor<>() {
            @Override
            public StoreSalesRank process(@NonNull Store store) {
                BigDecimal totalSales = batchSupport.paymentRepository
                        .sumAmountByStoreAndCreatedAtBetween(store, batchSupport.getStartDate(), batchSupport.getEndDate());
                Dashboard findDashboard = batchSupport.dashboardRepository
                        .findByStatisticsDateOrElseThrow(batchSupport.getYesterday());

                return StoreSalesRank.builder()
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .totalSales(totalSales == null ? BigDecimal.ZERO : totalSales)
                        .dashboard(findDashboard)
                        .ranking(0)
                        .build();
            }
        };
    }
}
