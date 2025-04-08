package com.example.wait4eat.domain.coupon.controller;

import com.example.wait4eat.domain.coupon.dto.response.CreateCouponResponse;
import com.example.wait4eat.domain.coupon.service.CouponService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/api/v1/coupons/{couponEventId}")
    public ResponseEntity<CreateCouponResponse> createCoupon(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long couponEventId
    ) {
        return ResponseEntity.ok(couponService.createCoupon(authUser, couponEventId));
    }
}
