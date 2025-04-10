package com.example.wait4eat.domain.notification.controller;

import com.example.wait4eat.domain.notification.dto.response.NotificationResponse;
import com.example.wait4eat.domain.notification.service.NotificationService;
import com.example.wait4eat.domain.notification.sse.SseEmitterManager;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;

    @GetMapping(value = "/api/v1/notifications/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestParam Long userId
    ) {
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
}
