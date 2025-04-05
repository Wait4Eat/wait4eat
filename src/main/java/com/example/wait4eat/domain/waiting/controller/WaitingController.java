package com.example.wait4eat.domain.waiting.controller;

import com.example.wait4eat.domain.waiting.dto.response.MyPastWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.MyWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.service.WaitingService;
import com.example.wait4eat.global.auth.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @GetMapping("/api/v1/stores/{storeId}/waitings")
    public ResponseEntity<Page<WaitingResponse>> getWaitings(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId,
            @RequestParam(required = false) WaitingStatus status,
            @PageableDefault(page = 1, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(waitingService.getWaitings(authUser.getUserId(), storeId, status, pageable));
    }

    @GetMapping("/api/v1/waitings/me")
    public ResponseEntity<MyWaitingResponse> getMyWaiting(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(waitingService.getMyWaiting(authUser.getUserId()));
    }

    @GetMapping("/api/v1/waitings/me/history")
    public ResponseEntity<Page<MyPastWaitingResponse>> getMyPastWaitings(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 1, size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(waitingService.getMyPastWaitings(authUser.getUserId(), pageable));
    }

    @DeleteMapping("/api/v1/waitings/{waitingId}")
    public ResponseEntity<Void> cancelMyWaiting(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long waitingId
    ) {
        waitingService.cancelMyWaiting(authUser.getUserId(), waitingId);
        return ResponseEntity.ok().build();
    }

}


