package com.example.wait4eat.domain.store.dto.response;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class GetStoreDetailResponse {

    private final Long id;
    private final Long userId;
    private final String name;
    private final String address;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final String description;
    private final int depositAmount;
    private final int waitingTeamCount;
    private final LocalDateTime createdAt;

    @Builder
    private GetStoreDetailResponse(
            Long id,
            Long userId,
            String name,
            String address,
            LocalTime openTime,
            LocalTime closeTime,
            String description,
            int depositAmount,
            int waitingTeamCount,
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
        this.waitingTeamCount = waitingTeamCount;
        this.createdAt = createdAt;
    }

    public static GetStoreDetailResponse of(Store store, User user) {
        return GetStoreDetailResponse.builder()
                .id(store.getId())
                .userId(user.getId())
                .name(store.getName())
                .address(store.getAddress())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .description(store.getDescription())
                .depositAmount(store.getDepositAmount())
                .waitingTeamCount(store.getWaitingTeamCount())
                .createdAt(store.getCreatedAt())
                .build();

    }
}
