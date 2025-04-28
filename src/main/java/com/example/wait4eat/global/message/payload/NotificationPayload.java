package com.example.wait4eat.global.message.payload;

import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.global.message.outbox.enums.AggregateType;
import com.example.wait4eat.global.util.IdGenerator;
import lombok.Getter;

@Getter
public class NotificationPayload implements MessagePayload {

    private final String messageKey;
    private final AggregateType aggregateType;
    private final Long notificationId;
    private final Long targetUserId;
    private final NotificationType notificationType;
    private final String message;

    public NotificationPayload(String messageKey, AggregateType aggregateType, Long notificationId, Long targetUserId, NotificationType notificationType, String message) {
        this.messageKey = messageKey;
        this.aggregateType = aggregateType;
        this.notificationId = notificationId;
        this.targetUserId = targetUserId;
        this.notificationType = notificationType;
        this.message = message;
    }

    public String getMessageKey() {
        return this.messageKey;
    }

    public Long getAggregateId() {
        return this.notificationId;
    }

    public String getAggregateType() {
        return this.aggregateType.name();
    }
}
