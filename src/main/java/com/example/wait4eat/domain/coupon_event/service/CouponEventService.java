package com.example.wait4eat.domain.coupon_event.service;

import com.example.wait4eat.domain.coupon_event.dto.request.CreateCouponEventRequest;
import com.example.wait4eat.domain.coupon_event.dto.response.CreateCouponEventResponse;
import com.example.wait4eat.domain.coupon_event.dto.response.GetCouponEventResponse;
import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import com.example.wait4eat.domain.coupon_event.event.CouponEventLaunchedEvent;
import com.example.wait4eat.domain.coupon_event.repository.CouponEventRepository;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponEventService {

    private final CouponEventRepository couponEventRepository;
    private final StoreRepository storeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateCouponEventResponse createCouponEvent(
            AuthUser authUser,
            Long storeId,
            CreateCouponEventRequest request
    ) {
        try {
            // 락 걸고 가게 조회
            Store store = storeRepository.findByIdWithPessimisticLock(storeId).orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));

            // 본인의 가게인지 검증
            if (!store.getUser().getId().equals(authUser.getUserId())) {
                throw new CustomException(ExceptionType.STORE_NOT_MATCH_USER);
            }

            // 쿠폰이벤트는 한 번만 생성 가능(이미 진행 중인 쿠폰이벤트가 있으면 예외 처리, 1가게 1쿠폰이기에 storeId로 유무 검증)
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

            eventPublisher.publishEvent(CouponEventLaunchedEvent.of(store, savedCouponEvent));

            return CreateCouponEventResponse.from(savedCouponEvent);
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new CustomException(ExceptionType.DATABASE_LOCK_FAILED);
        }
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

        return GetCouponEventResponse.from(couponEvent);
    }

}

