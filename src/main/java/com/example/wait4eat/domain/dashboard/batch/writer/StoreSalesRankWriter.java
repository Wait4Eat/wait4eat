package com.example.wait4eat.domain.dashboard.batch.writer;

import com.example.wait4eat.domain.dashboard.batch.DashboardBatchSupport;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class StoreSalesRankWriter implements ItemWriter<StoreSalesRank> {
    private final DashboardBatchSupport batchSupport;
    private int rank = 1;

    public StoreSalesRankWriter(DashboardBatchSupport batchSupport) {
        this.batchSupport = batchSupport;
    }

    @Override
    public void write(Chunk<? extends StoreSalesRank> items) {
        for (StoreSalesRank salesRank : items) {
            salesRank.setRanking(rank);
            rank++;
        }

        batchSupport.getStoreSalesRankRepository().saveAll(items);
    }
}
