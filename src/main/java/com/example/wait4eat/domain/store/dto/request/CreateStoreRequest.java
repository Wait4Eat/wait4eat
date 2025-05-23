package com.example.wait4eat.domain.store.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
public class CreateStoreRequest {

    @NotBlank(message = "가게 이름 입력은 필수입니다.")
    private String name;

    @NotBlank(message = "가게 주소 입력은 필수입니다.")
    private String address;

    @NotNull(message = "가게 오픈 시간 입력은 필수입니다.")
    private LocalTime openTime;

    @NotNull(message = "가게 마감 시간 입력은 필수입니다.")
    private LocalTime closeTime;

    private String description;

    @NotNull(message = "예약금 입력은 필수입니다.")
    private int depositAmount;

    @Builder
    public CreateStoreRequest(
            String name,
            String address,
            LocalTime openTime,
            LocalTime closeTime,
            String description,
            int depositAmount
    ) {
        this.name = name;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description;
        this.depositAmount = depositAmount;
    }
}
