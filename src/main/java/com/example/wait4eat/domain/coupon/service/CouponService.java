package com.example.wait4eat.domain.coupon.service;

import com.example.wait4eat.domain.coupon.dto.response.CreateCouponResponse;
import com.example.wait4eat.domain.coupon.dto.response.GetAllCouponResponse;
import com.example.wait4eat.domain.coupon.dto.response.GetOneCouponResponse;
import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.coupon.repository.CouponRepository;
import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import com.example.wait4eat.domain.coupon_event.repository.CouponEventRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponEventRepository couponEventRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateCouponResponse createCoupon(AuthUser authUser, Long couponEventId) {
        try {
            User user = getUserByAuthUser(authUser);

            // 락 걸고 쿠폰 이벤트 조회
            CouponEvent couponEvent = couponEventRepository.findByIdWithPessimisticLock(couponEventId)
                    .orElseThrow(() -> new CustomException(ExceptionType.COUPON_EVENT_NOT_FOUND));

            // 중복 발급 검증(쿠폰은 한 번만 받을 수 있음)
            if (couponRepository.existsByUserIdAndCouponEventId(user.getId(), couponEventId)) {
                throw new CustomException(ExceptionType.COUPON_ALREADY_EXISTS);
            }

            // 소진된 쿠폰 검증
            if (couponEvent.getIssuedQuantity() >= couponEvent.getTotalQuantity()) {
                throw new CustomException(ExceptionType.COUPON_SOLD_OUT);
            }

            // 쿠폰이벤트 issuedQuantity 수량 변경
            couponEvent.increaseIssuedQuantity();
            couponEventRepository.save(couponEvent);

            Coupon coupon = Coupon.builder()
                    .user(user)
                    .couponEvent(couponEvent)
                    .discountAmount(couponEvent.getDiscountAmount())
                    .expiresAt(couponEvent.getExpiresAt())
                    .issuedAt(LocalDateTime.now())
                    .isUsed(false)
                    .usedAt(null)
                    .build();

            Coupon savedCoupon = couponRepository.save(coupon);

            return CreateCouponResponse.from(savedCoupon);
        } catch (PessimisticLockException | LockTimeoutException e) {
            throw new CustomException(ExceptionType.DATABASE_LOCK_FAILED);
        }
    }

    @Transactional(readOnly = true)
    public Page<GetAllCouponResponse> getAllCoupon(AuthUser authUser, Pageable pageable) {

        User user = getUserByAuthUser(authUser);

        Page<Coupon> couponPage = couponRepository.findAllByUser(user, pageable);

        return couponPage.map(GetAllCouponResponse::from);
    }

    @Transactional(readOnly = true)
    public GetOneCouponResponse getCouponByCouponEvent(AuthUser authUser, Long couponEventId) {

        User user = getUserByAuthUser(authUser);

        // 조회 요청한 쿠폰이벤트가 있는지 검증
        if (!couponEventRepository.existsById(couponEventId)) {
            throw new CustomException(ExceptionType.COUPON_EVENT_NOT_FOUND);
        }

        Coupon coupon = couponRepository.findByUserIdAndCouponEventId(user.getId(), couponEventId);

        // 발급받지 않은 쿠폰 검증
        if (coupon == null) {
            throw new CustomException(ExceptionType.COUPON_NOT_FOUND);
        }

        return GetOneCouponResponse.from(coupon);
    }

    private User getUserByAuthUser(AuthUser authUser) {
        return userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));
    }

}
