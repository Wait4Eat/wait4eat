package com.example.wait4eat.domain.notification.service;

import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    public void send(Long userId, String storeName) {
        Notification notification = notificationService.create(
                userId,
                NotificationType.COUPON_EVENT_LAUNCHED,
                "[" + storeName + "] " + NotificationType.COUPON_EVENT_LAUNCHED.getMessage()
        );
        eventPublisher.publishEvent(NotificationEvent.from(notification));
    }
}