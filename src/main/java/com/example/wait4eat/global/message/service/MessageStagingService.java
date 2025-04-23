package com.example.wait4eat.global.message.service;

import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.notification.service.NotificationService;
import com.example.wait4eat.global.message.dto.NotificationMessagePublishRequest;
import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import com.example.wait4eat.global.message.outbox.service.OutboxService;
import com.example.wait4eat.global.message.payload.NotificationPayload;
import com.example.wait4eat.global.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageStagingService {

    private final NotificationService notificationService;
    private final OutboxService outboxService;

    @Transactional
    public List<OutboxMessage> stage(NotificationMessagePublishRequest request) {
        List<Notification> notifications =
                notificationService.createBulk(request.getTargetUsers(), request.getNotificationType(), request.getMessage());

        List<NotificationPayload> payloads = notifications.stream()
                .map(notification -> new NotificationPayload(
                        IdGenerator.generateMessageId(),
                        notification.getId(),
                        notification.getUser().getId(),
                        notification.getType(),
                        notification.getText()
                ))
                .toList();

        // Outbox 저장
        List<OutboxMessage> outboxes = outboxService.createNotificationOutboxes(payloads);

        return outboxes;
    }
}
