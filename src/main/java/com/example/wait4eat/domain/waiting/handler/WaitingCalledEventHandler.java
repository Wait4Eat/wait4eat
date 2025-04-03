package com.example.wait4eat.domain.waiting.handler;

import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.notification.event.NotificationEvent;
import com.example.wait4eat.domain.notification.service.NotificationService;
import com.example.wait4eat.domain.waiting.event.WaitingCalledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WaitingCalledEventHandler {

    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(WaitingCalledEvent event) {
        Notification notification = notificationService.create(
                event.getUserId(),
                NotificationType.WAITING_CALLED
        );
        eventPublisher.publishEvent(NotificationEvent.from(notification));
    }
}
