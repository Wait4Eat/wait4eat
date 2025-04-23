package com.example.wait4eat.domain.coupon_event.event.handler;

import com.example.wait4eat.domain.coupon_event.event.CouponEventLaunchedEvent;
import com.example.wait4eat.global.message.dto.NotificationMessagePublishRequest;
import com.example.wait4eat.global.message.enums.MessageType;
import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import com.example.wait4eat.global.message.outbox.event.OutboxSavedEvent;
import com.example.wait4eat.global.message.service.MessageStagingService;
import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.storewishlist.repository.StoreWishlistRepository;
import com.example.wait4eat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventLaunchedEventHandler {

    private final StoreWishlistRepository storeWishlistRepository;
    private final MessageStagingService messageStagingService;
    private final ApplicationEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(CouponEventLaunchedEvent event) {
        List<User> users = storeWishlistRepository.findAllUsersByStoreId(event.getStoreId());

        String message = "[" + event.getStoreName() + "] " + NotificationType.COUPON_EVENT_LAUNCHED.getMessage();
        NotificationMessagePublishRequest notificationMessagePublishRequest = new NotificationMessagePublishRequest(
                MessageType.COUPON_EVENT_LAUNCHED,
                message,
                users,
                NotificationType.COUPON_EVENT_LAUNCHED
        );

        List<OutboxMessage> outboxes = messageStagingService.stage(notificationMessagePublishRequest);
        eventPublisher.publishEvent(new OutboxSavedEvent(outboxes));
    }
}
