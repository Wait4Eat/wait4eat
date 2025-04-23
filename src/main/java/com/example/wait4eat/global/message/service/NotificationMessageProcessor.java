package com.example.wait4eat.global.message.service;

import com.example.wait4eat.domain.notification.publisher.NotificationPublisher;
import com.example.wait4eat.global.message.dedup.MessageDeduplicationHandler;
import com.example.wait4eat.global.message.payload.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationMessageProcessor {

    private final NotificationPublisher notificationPublisher;
    private final MessageDeduplicationHandler messageDeduplicationHandler;

    @Transactional
    public void handleNotificationPublish(NotificationPayload payload) {
        notificationPublisher.publish(payload);
        messageDeduplicationHandler.markAsProcessed(payload.getMessageKey());
    }
}
