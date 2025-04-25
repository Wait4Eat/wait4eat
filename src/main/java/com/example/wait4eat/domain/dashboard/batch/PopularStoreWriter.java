package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.dto.StoreWaitingStats;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@StepScope
public class PopularStoreWriter implements ItemWriter<StoreWaitingStats> {

    private final List<StoreWaitingStats> buffer = new ArrayList<>();
    private final DashboardBatchSupport batchSupport;

    public PopularStoreWriter(DashboardBatchSupport batchSupport) {
        this.batchSupport = batchSupport;
    }

    @Override
    public void write(Chunk<? extends StoreWaitingStats> items) throws Exception {
        buffer.addAll(items.getItems());
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        List<StoreWaitingStats> top10 = buffer.stream()
                .sorted(Comparator.comparing(StoreWaitingStats::getWaitingCount).reversed())
                .limit(10)
                .toList();

        Dashboard dashboard = batchSupport.dashboardRepository
                .findByStatisticsDateOrElseThrow(batchSupport.getYesterday());

        List<PopularStore> result = new ArrayList<>();
        int rank = 1;
        for (StoreWaitingStats stats : top10) {
            result.add(PopularStore.builder()
                    .storeId(stats.getStore().getId())
                    .storeName(stats.getStore().getName())
                    .waitingCount(stats.getWaitingCount())
                    .ranking(rank++)
                    .dashboard(dashboard)
                    .build());
        }

        batchSupport.popularStoreRepository.saveAll(result);
    }
}
