package com.example.wait4eat.domain.dashboard.dto;

import com.example.wait4eat.domain.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreWaitingStats {
    private final Store store;
    private final Long waitingCount;

    @Builder
    private StoreWaitingStats(Store store, Long waitingCount) {
        this.store = store;
        this.waitingCount = waitingCount;
    }
}
