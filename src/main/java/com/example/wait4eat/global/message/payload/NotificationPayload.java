package com.example.wait4eat.global.message.payload;

import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.global.util.IdGenerator;
import lombok.Getter;

@Getter
public class NotificationPayload implements MessagePayload {

    private final String messageKey;
    private final Long notificationId;
    private final Long targetUserId;
    private final NotificationType notificationType;
    private final String message;

    public NotificationPayload(String messageKey, Long notificationId, Long targetUserId, NotificationType notificationType, String message) {
        this.messageKey = messageKey;
        this.notificationId = notificationId;
        this.targetUserId = targetUserId;
        this.notificationType = notificationType;
        this.message = message;
    }

    public String getMessageKey() {
        return this.messageKey;
    }
}
