package com.example.wait4eat.domain.coupon_event.controller;

import com.example.wait4eat.domain.coupon_event.dto.request.CreateCouponEventRequest;
import com.example.wait4eat.domain.coupon_event.dto.response.CreateCouponEventResponse;
import com.example.wait4eat.domain.coupon_event.dto.response.GetCouponEventResponse;
import com.example.wait4eat.domain.coupon_event.service.CouponEventService;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CouponEventController {

    private final CouponEventService couponEventService;

    // 사장 권한 추가 예정
    @PostMapping({"/api/v1/stores/{storeId}/couponevents"})
    public ResponseEntity<CreateCouponEventResponse> createCouponEvent(
            @PathVariable Long storeId,
            @Valid @RequestBody CreateCouponEventRequest request
    ) {
        return ResponseEntity.ok(couponEventService.createCouponEvent(storeId, request));
    }

    @GetMapping("/api/v1/stores/{storeId}/couponevents/{couponEventId}")
    public ResponseEntity<GetCouponEventResponse> getCouponEvent(
            @PathVariable Long storeId,
            @PathVariable Long couponEventId
    ) {
        return ResponseEntity.ok(couponEventService.getCouponEvent(storeId, couponEventId));
    }
}
