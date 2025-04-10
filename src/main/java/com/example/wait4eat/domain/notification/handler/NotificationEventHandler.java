package com.example.wait4eat.domain.notification.handler;

import com.example.wait4eat.domain.notification.event.NotificationEvent;
import com.example.wait4eat.domain.notification.publisher.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final NotificationPublisher notificationPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationEvent event) {
        log.info("이제 여기서 실제 알림 전송 로직 호출");
        notificationPublisher.publish(event);
    }
}
