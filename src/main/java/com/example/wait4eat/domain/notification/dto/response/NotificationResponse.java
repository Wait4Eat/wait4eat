package com.example.wait4eat.domain.notification.dto.response;

import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {

    private final Long id;
    private final NotificationType type;
    private final Boolean isRead;
    private final String text;
    private final LocalDateTime createdAt;

    @Builder
    private NotificationResponse(Long id, NotificationType type, Boolean isRead, String text, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.isRead = isRead;
        this.text = text;
        this.createdAt = createdAt;
    }

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .text(notification.getText())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
