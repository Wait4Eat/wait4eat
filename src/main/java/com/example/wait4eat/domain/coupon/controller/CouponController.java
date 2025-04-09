package com.example.wait4eat.domain.coupon.controller;

import com.example.wait4eat.domain.coupon.dto.response.CreateCouponResponse;
import com.example.wait4eat.domain.coupon.dto.response.GetAllCouponResponse;
import com.example.wait4eat.domain.coupon.dto.response.GetOneCouponResponse;
import com.example.wait4eat.domain.coupon.service.CouponService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.dto.response.PageResponse;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/api/v1/coupons/{couponEventId}")
    public ResponseEntity<SuccessResponse<CreateCouponResponse>> createCoupon(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long couponEventId
    ) {
        return ResponseEntity.ok(SuccessResponse.from(couponService.createCoupon(authUser, couponEventId)));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/api/v1/coupons")
    public ResponseEntity<PageResponse<GetAllCouponResponse>> getAllCoupon(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 10, sort = "issuedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(couponService.getAllCoupon(authUser, pageable)));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/api/v1/coupons/{couponEventId}")
    public ResponseEntity<SuccessResponse<GetOneCouponResponse>> getOneCoupon(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long couponEventId
    ) {
        return ResponseEntity.ok(SuccessResponse.from(couponService.getOneCoupon(authUser, couponEventId)));
    }

}
