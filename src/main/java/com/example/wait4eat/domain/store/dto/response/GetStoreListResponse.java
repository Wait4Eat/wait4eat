package com.example.wait4eat.domain.store.dto.response;

import com.example.wait4eat.domain.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class GetStoreListResponse {

    private Long id;
    private String name;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String imageUrl;
    private int depositAmount;

    @Builder
    private GetStoreListResponse(
            Long id,
            String name,
            String address,
            LocalTime openTime,
            LocalTime closeTime,
            String imageUrl,
            int depositAmount
    ) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.imageUrl = imageUrl;
        this.depositAmount = depositAmount;
    }

    public static GetStoreListResponse from(Store store) {
        return GetStoreListResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .imageUrl(store.getImageUrl())
                .depositAmount(store.getDepositAmount())
                .build();
    }
}
