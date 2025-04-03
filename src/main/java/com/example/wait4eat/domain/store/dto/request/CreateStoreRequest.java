package com.example.wait4eat.domain.store.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class CreateStoreRequest {

    private String name;
    private String address;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String description;
    private String imageUrl;
    private int depositAmount;

    @Builder
    public CreateStoreRequest(
            String name,
            String address,
            LocalTime openTime,
            LocalTime closeTime,
            String description,
            String imageUrl,
            int depositAmount
    ) {
        this.name = name;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description;
        this.imageUrl = imageUrl;
        this.depositAmount = depositAmount;
    }
}
