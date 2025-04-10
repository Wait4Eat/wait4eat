package com.example.wait4eat.domain.store.dto.response;

import com.example.wait4eat.domain.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class GetStoreListResponse {

    private final Long id;
    private final String name;
    private final String address;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final int depositAmount;
    private final int waitingTeamCount;

    @Builder
    private GetStoreListResponse(
            Long id,
            String name,
            String address,
            LocalTime openTime,
            LocalTime closeTime,
            int depositAmount,
            int waitingTeamCount
    ) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.depositAmount = depositAmount;
        this.waitingTeamCount = waitingTeamCount;
    }

    public static GetStoreListResponse from(Store store) {
        return GetStoreListResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .depositAmount(store.getDepositAmount())
                .waitingTeamCount(store.getWaitingTeamCount())
                .build();
    }
}
