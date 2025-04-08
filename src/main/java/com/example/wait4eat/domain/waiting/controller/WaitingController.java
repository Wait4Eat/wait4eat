package com.example.wait4eat.domain.waiting.controller;

import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.waiting.dto.request.CreateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.request.UpdateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.response.*;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.service.WaitingService;
import com.example.wait4eat.global.auth.dto.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingService waitingService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/api/v1/stores/{storeId}/waitings")
    public ResponseEntity<CreateWaitingResponse> createWaiting(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestBody @Valid CreateWaitingRequest request
    ) {
        return ResponseEntity.ok(waitingService.createWaiting(authUser.getUserId(), storeId, request));
    }

    @Secured(UserRole.Authority.OWNER)
    @GetMapping("/api/v1/stores/{storeId}/waitings")
    public ResponseEntity<Page<WaitingResponse>> getWaitings(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable long storeId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) WaitingStatus status
    ) {
        return ResponseEntity.ok(waitingService.getWaitings(authUser.getUserId(), storeId, status, pageable));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/api/v1/waitings/me")
    public ResponseEntity<MyWaitingResponse> getMyWaiting(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(waitingService.getMyWaiting(authUser.getUserId()));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/api/v1/waitings/me/history")
    public ResponseEntity<Page<MyPastWaitingResponse>> getMyPastWaitings(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(waitingService.getMyPastWaitings(authUser.getUserId(), pageable));
    }

    @Secured(UserRole.Authority.USER)
    @DeleteMapping("/api/v1/waitings/{waitingId}")
    public ResponseEntity<Void> cancelMyWaiting(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long waitingId
    ) {
        waitingService.cancelMyWaiting(authUser.getUserId(), waitingId);
        return ResponseEntity.ok().build();
    }

    @Secured(UserRole.Authority.OWNER)
    @PatchMapping("/api/v1/waitings/{waitingId}/status")
    public ResponseEntity<UpdateWaitingResponse> updateWaitingStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long waitingId,
            @RequestBody @Valid UpdateWaitingRequest updateWaitingRequest
    ) {
        return ResponseEntity.ok(waitingService.updateWaitingStatus(authUser.getUserId(), waitingId, updateWaitingRequest));
    }

}


