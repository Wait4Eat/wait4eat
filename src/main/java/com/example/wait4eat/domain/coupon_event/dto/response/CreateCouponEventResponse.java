package com.example.wait4eat.domain.coupon_event.dto.response;

import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateCouponEventResponse {

    private final Long id;
    private final Long storeId;
    private final String name;
    private final BigDecimal discountAmount;
    private final Integer totalQuantity;
    private final Integer issuedQuantity;
    private final LocalDateTime expiresAt;
    private final LocalDateTime createdAt;

    @Builder
    private CreateCouponEventResponse(
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

    public static CreateCouponEventResponse from(CouponEvent couponEvent) {
        return CreateCouponEventResponse.builder()
                .id(couponEvent.getId())
                .storeId(couponEvent.getStore().getId())
                .name(couponEvent.getName())
                .discountAmount(couponEvent.getDiscountAmount())
                .totalQuantity(couponEvent.getTotalQuantity())
                .issuedQuantity(couponEvent.getIssuedQuantity())
                .expiresAt(couponEvent.getExpiresAt())
                .createdAt(couponEvent.getCreatedAt())
                .build();
    }

}
