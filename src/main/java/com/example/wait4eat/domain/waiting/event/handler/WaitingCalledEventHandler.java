package com.example.wait4eat.domain.waiting.event.handler;

import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import com.example.wait4eat.global.message.dto.NotificationMessagePublishRequest;
import com.example.wait4eat.global.message.enums.MessageType;
import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.waiting.event.WaitingCalledEvent;
import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import com.example.wait4eat.global.message.outbox.event.OutboxSavedEvent;
import com.example.wait4eat.global.message.service.MessageStagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WaitingCalledEventHandler {

    private final UserRepository userRepository;
    private final MessageStagingService messageStagingService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(WaitingCalledEvent event) {
        User user = userRepository.findById(event.getUserId())
                    .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));

        NotificationMessagePublishRequest notificationMessagePublishRequest = new NotificationMessagePublishRequest(
                MessageType.WAITING_CALLED,
                NotificationType.COUPON_EVENT_LAUNCHED.getMessage(),
                List.of(user),
                NotificationType.COUPON_EVENT_LAUNCHED
        );

        List<OutboxMessage> outboxes = messageStagingService.stage(notificationMessagePublishRequest);

        applicationEventPublisher.publishEvent(new OutboxSavedEvent(outboxes));
    }
}
