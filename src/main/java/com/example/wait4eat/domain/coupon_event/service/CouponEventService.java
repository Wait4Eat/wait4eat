package com.example.wait4eat.domain.coupon_event.service;

import com.example.wait4eat.domain.coupon_event.dto.request.CreateCouponEventRequest;
import com.example.wait4eat.domain.coupon_event.dto.response.CreateCouponEventResponse;
import com.example.wait4eat.domain.coupon_event.dto.response.GetCouponEventResponse;
import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import com.example.wait4eat.domain.coupon_event.repository.CouponEventRepository;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponEventService {

    private final CouponEventRepository couponEventRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public CreateCouponEventResponse createCouponEvent(
            Long storeId,
            CreateCouponEventRequest request
    ) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));

        // 쿠폰이벤트는 한 번만 생성 가능(이미 진행 중인 쿠폰 이벤트가 있으면 예외 처리)
        boolean existsCouponEvent = couponEventRepository.existsByStoreId(storeId);
        if (existsCouponEvent) {
            throw new CustomException(ExceptionType.COUPON_EVENT_ALREADY_EXISTS);
        }

        CouponEvent couponEvent = CouponEvent.builder()
                .store(store)
                .name(request.getName())
                .discountAmount(request.getDiscountAmount())
                .totalQuantity(request.getTotalQuantity())
                .issuedQuantity(0)
                .expiresAt(request.getExpiresAt())
                .createdAt(LocalDateTime.now())
                .build();

        CouponEvent savedCouponEvent = couponEventRepository.save(couponEvent);

        return CreateCouponEventResponse.builder()
                .id(savedCouponEvent.getId())
                .storeId(savedCouponEvent.getStore().getId())
                .name(savedCouponEvent.getName())
                .discountAmount(savedCouponEvent.getDiscountAmount())
                .totalQuantity(savedCouponEvent.getTotalQuantity())
                .issuedQuantity(savedCouponEvent.getIssuedQuantity())
                .expiresAt(savedCouponEvent.getExpiresAt())
                .createdAt(savedCouponEvent.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public GetCouponEventResponse getCouponEvent(
            Long storeId,
            Long couponEventId
    ) {
        CouponEvent couponEvent = couponEventRepository.findById(couponEventId).orElseThrow(() -> new CustomException(ExceptionType.COUPON_EVENT_NOT_FOUND));

        if (!storeId.equals(couponEvent.getStore().getId())) {
            throw new CustomException(ExceptionType.COUPON_EVENT_NOT_MATCH_STORE);
        }

        return GetCouponEventResponse.builder()
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
