package com.example.wait4eat.domain.notification.publisher;

import com.example.wait4eat.domain.notification.event.NotificationEvent;

public interface NotificationPublisher {
    void publish(NotificationEvent notificationEvent);
}
