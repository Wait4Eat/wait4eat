package com.example.wait4eat.domain.coupon_event.event;

import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import com.example.wait4eat.domain.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CouponEventLaunchedEvent {

    private final Long storeId;
    private final String storeName;
    private final Long couponEventId;

    @Builder
    private CouponEventLaunchedEvent(Long storeId, String storeName, Long couponEventId) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.couponEventId = couponEventId;
    }

    public static CouponEventLaunchedEvent of(Store store, CouponEvent couponEvent) {
        return CouponEventLaunchedEvent.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .couponEventId(couponEvent.getId())
                .build();
    }
}
