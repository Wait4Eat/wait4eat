package com.example.wait4eat.domain.dashboard.batch.writer;

import com.example.wait4eat.domain.dashboard.batch.DashboardBatchSupport;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class StoreSalesStatsWriter implements ItemWriter<StoreSalesRank> {
    private final DashboardBatchSupport batchSupport;

    public StoreSalesStatsWriter(DashboardBatchSupport batchSupport) {
        this.batchSupport = batchSupport;
    }

    @Override
    public void write(Chunk<? extends StoreSalesRank> items) {
        batchSupport.getStoreSalesRankRepository().saveAll(items.getItems());
    }
}
