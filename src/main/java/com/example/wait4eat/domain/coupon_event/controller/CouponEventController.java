package com.example.wait4eat.domain.coupon_event.controller;

import com.example.wait4eat.domain.coupon_event.dto.request.CreateCouponEventRequest;
import com.example.wait4eat.domain.coupon_event.dto.response.CreateCouponEventResponse;
import com.example.wait4eat.domain.coupon_event.dto.response.GetCouponEventResponse;
import com.example.wait4eat.domain.coupon_event.service.CouponEventService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CouponEventController {

    private final CouponEventService couponEventService;

    @Secured(UserRole.Authority.OWNER)
    @PostMapping({"/api/v1/stores/{storeId}/couponevents"})
    public ResponseEntity<SuccessResponse<CreateCouponEventResponse>> createCouponEvent(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @Valid @RequestBody CreateCouponEventRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.from(couponEventService.createCouponEvent(authUser, storeId, request)));
    }

    @GetMapping("/api/v1/stores/{storeId}/couponevents/{couponEventId}")
    public ResponseEntity<SuccessResponse<GetCouponEventResponse>> getCouponEvent(
            @PathVariable Long storeId,
            @PathVariable Long couponEventId
    ) {
        return ResponseEntity.ok(SuccessResponse.from(couponEventService.getCouponEvent(storeId, couponEventId)));
    }

}
