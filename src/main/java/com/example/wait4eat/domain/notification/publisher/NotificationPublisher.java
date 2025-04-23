package com.example.wait4eat.domain.notification.publisher;

import com.example.wait4eat.global.message.payload.NotificationPayload;

public interface NotificationPublisher {
    void publish(NotificationPayload notificationPayload);
}
