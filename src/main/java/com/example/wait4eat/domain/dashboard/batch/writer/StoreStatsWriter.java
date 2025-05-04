package com.example.wait4eat.domain.dashboard.batch.writer;

import com.example.wait4eat.domain.store.entity.Store;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@StepScope
public class StoreStatsWriter implements ItemWriter<Store>, StepExecutionListener {
    private Long totalStoreCount = 0L;
    private Long dailyNewStoreCount = 0L;

    @Override
    public void write(Chunk<? extends Store> stores) {
        for (Store store : stores) {
            totalStoreCount++;
            if (isCreatedYesterday(store)) {
                dailyNewStoreCount++;
            }
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().putLong("totalStoreCount", totalStoreCount);
        stepExecution.getExecutionContext().putLong("dailyNewStoreCount", dailyNewStoreCount);
        return ExitStatus.COMPLETED;
    }

    private boolean isCreatedYesterday(Store store) {
        return store.getCreatedAt().toLocalDate().equals(LocalDate.now().minusDays(1));
    }
}
