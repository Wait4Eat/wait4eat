package com.example.wait4eat.domain.notification.controller;

import com.example.wait4eat.domain.notification.dto.response.NotificationResponse;
import com.example.wait4eat.domain.notification.service.NotificationService;
import com.example.wait4eat.domain.notification.sse.SseEmitterManager;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.auth.jwt.JwtUtil;
import com.example.wait4eat.global.dto.response.PageResponse;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;
    private final JwtUtil jwtUtil;

    @GetMapping("/api/v1/notifications/token")
    public ResponseEntity<SuccessResponse> getSseToken(@AuthenticationPrincipal AuthUser authUser) {
        String sseToken = jwtUtil.createSseToken(authUser.getUserId());
        return ResponseEntity.ok(SuccessResponse.from(sseToken));
    }

    @GetMapping(value = "/api/v1/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestParam String token
    ) {
        Claims claims = jwtUtil.extractClaims(token);
        jwtUtil.validateScope(claims, "sse");
        Long userId = Long.parseLong(claims.getSubject());
        return sseEmitterManager.connect(userId);
    }

    @GetMapping("/api/v1/notifications")
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                PageResponse.from(notificationService.getNotifications(authUser.getUserId(), pageable))
        );
    }

    @PatchMapping("/api/v1/notifications/{notificationId}")
    public ResponseEntity<SuccessResponse> markAsRead(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long notificationId
    ) {
        notificationService.markAsRead(authUser.getUserId(), notificationId);
        return ResponseEntity.ok(SuccessResponse.from(notificationId));
    }
}
