package com.example.wait4eat.domain.coupon.dto.response;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class GetOneCouponResponse {

    private final Long id;
    private final Long userId;
    private final String storeName;
    private final String couponEventName;
    private final BigDecimal discountAmount;
    private final LocalDateTime expiresAt;
    private final LocalDateTime issuedAt;
    private final Boolean isUsed;
    private final LocalDateTime usedAt;

    @Builder
    private GetOneCouponResponse(
            Long id,
            Long userId,
            String storeName,
            String couponEventName,
            BigDecimal discountAmount,
            LocalDateTime expiresAt,
            LocalDateTime issuedAt,
            Boolean isUsed,
            LocalDateTime usedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.storeName = storeName;
        this.couponEventName = couponEventName;
        this.discountAmount = discountAmount;
        this.expiresAt = expiresAt;
        this.issuedAt = issuedAt;
        this.isUsed = isUsed;
        this.usedAt = usedAt;
    }

    public static GetOneCouponResponse from(Coupon coupon) {
        return GetOneCouponResponse.builder()
                .id(coupon.getId())
                .userId(coupon.getUser().getId())
                .storeName(coupon.getCouponEvent().getStore().getName())
                .couponEventName(coupon.getCouponEvent().getName())
                .discountAmount(coupon.getDiscountAmount())
                .expiresAt(coupon.getExpiresAt())
                .issuedAt(coupon.getIssuedAt())
                .isUsed(coupon.getIsUsed())
                .usedAt(coupon.getUsedAt())
                .build();
    }
}
