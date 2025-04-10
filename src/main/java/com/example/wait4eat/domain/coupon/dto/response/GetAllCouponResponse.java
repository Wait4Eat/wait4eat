package com.example.wait4eat.domain.coupon.dto.response;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class GetAllCouponResponse {

    private final Long id;
    private final String storeName;
    private final String couponEventName;
    private final BigDecimal discountAmount;

    @Builder
    private GetAllCouponResponse(
            Long id,
            String storeName,
            String couponEventName,
            BigDecimal discountAmount
    ) {
        this.id = id;
        this.storeName = storeName;
        this.couponEventName = couponEventName;
        this.discountAmount = discountAmount;
    }

    public static GetAllCouponResponse from(Coupon coupon) {
        return GetAllCouponResponse.builder()
                .id(coupon.getId())
                .storeName(coupon.getCouponEvent().getStore().getName())
                .couponEventName(coupon.getCouponEvent().getName())
                .discountAmount(coupon.getDiscountAmount())
                .build();

    }
}


