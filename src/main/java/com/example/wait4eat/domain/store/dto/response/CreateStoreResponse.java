package com.example.wait4eat.domain.store.dto.response;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class CreateStoreResponse {

    private final Long id;
    private final Long userId;
    private final String name;
    private final String address;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final String description;
    private final int depositAmount;
    private final LocalDateTime createdAt;

    @Builder
    private CreateStoreResponse(
            Long id,
            Long userId,
            String name,
            String address,
            LocalTime openTime,
            LocalTime closeTime,
            String description,
            int depositAmount,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description;
        this.depositAmount = depositAmount;
        this.createdAt = createdAt;
    }

    public static CreateStoreResponse of(Store store, User user) {
        return CreateStoreResponse.builder()
                .id(store.getId())
                .userId(user.getId())
                .name(store.getName())
                .address(store.getAddress())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .description(store.getDescription())
                .depositAmount(store.getDepositAmount())
                .createdAt(store.getCreatedAt())
                .build();

    }
}
