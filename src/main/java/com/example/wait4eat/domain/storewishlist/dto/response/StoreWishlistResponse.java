package com.example.wait4eat.domain.storewishlist.dto.response;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.storewishlist.entity.StoreWishlist;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StoreWishlistResponse {
    private final Long id;
    private final Store store;
    private final LocalDateTime createdAt;

    @Builder
    private StoreWishlistResponse(Long id, Store store, LocalDateTime createdAt) {
        this.id = id;
        this.store = store;
        this.createdAt = createdAt;
    }

    public static StoreWishlistResponse from(StoreWishlist storeWishlist) {
        return StoreWishlistResponse.builder()
                .id(storeWishlist.getId())
                .store(storeWishlist.getStore())
                .createdAt(storeWishlist.getCreatedAt())
                .build();
    }
}
