package com.example.wait4eat.domain.dashboard.dto;

import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import lombok.Builder;
import lombok.Getter;

@Getter
public class PopularStoreResponse {
    private final Long id;
    private final Long storeId;
    private final String storeName;
    private final Long waitingCount;
    private final int ranking;

    @Builder
    private PopularStoreResponse(Long id, Long storeId, String storeName, Long waitingCount, int ranking) {
        this.id = id;
        this.storeId = storeId;
        this.storeName = storeName;
        this.waitingCount = waitingCount;
        this.ranking = ranking;
    }

    public static PopularStoreResponse from(PopularStore popularStore) {
        return PopularStoreResponse.builder()
                .id(popularStore.getId())
                .storeId(popularStore.getStoreId())
                .storeName(popularStore.getStoreName())
                .waitingCount(popularStore.getWaitingCount())
                .ranking(popularStore.getRanking())
                .build();
    }
}
