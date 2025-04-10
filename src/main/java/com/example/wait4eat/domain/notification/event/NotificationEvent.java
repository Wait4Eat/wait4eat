package com.example.wait4eat.domain.notification.event;

import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.notification.enums.NotificationType;
import lombok.Builder;

import lombok.Getter;

@Getter
public class NotificationEvent {

    private final Long userId;
    private final NotificationType type;
    private final String message;

    @Builder
    private NotificationEvent(Long userId, NotificationType type, String message) {
        this.userId = userId;
        this.type = type;
        this.message = message;
    }

    public static NotificationEvent from(Notification notification) {
        return NotificationEvent.builder()
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .message(notification.getText())
                .build();
    }
}
