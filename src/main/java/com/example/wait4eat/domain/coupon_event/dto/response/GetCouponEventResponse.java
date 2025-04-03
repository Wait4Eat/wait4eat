package com.example.wait4eat.domain.coupon_event.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GetCouponEventResponse {

    private final Long id;
    private final Long storeId;
    private final String name;
    private final BigDecimal discountAmount;
    private final Integer totalQuantity;
    private final Integer issuedQuantity;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    @Builder
    public GetCouponEventResponse(
            Long id,
            Long storeId,
            String name,
            BigDecimal discountAmount,
            Integer totalQuantity,
            Integer issuedQuantity,
            LocalDateTime expiresAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.discountAmount = discountAmount;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = issuedQuantity;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }
}
