package com.example.wait4eat.domain.payment.event.handler;


import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.payment.event.PaymentRefundRequestedEvent;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.global.message.dto.EventMessagePublishRequest;
import com.example.wait4eat.global.message.enums.MessageType;
import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import com.example.wait4eat.global.message.outbox.enums.AggregateType;
import com.example.wait4eat.global.message.outbox.event.OutboxSavedEvent;
import com.example.wait4eat.global.message.service.MessageStagingService;
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
public class PaymentRefundRequestedEventHandler {

    private final PaymentRepository paymentRepository;
    private final MessageStagingService messageStagingService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PaymentRefundRequestedEvent event) {
        Payment payment = paymentRepository.findByOrderIdAndStatus(event.getOrderId(), PaymentStatus.SUCCEEDED);

        EventMessagePublishRequest request = new EventMessagePublishRequest(
                AggregateType.PAYMENT,
                MessageType.PAYMENT_REFUND_REQUESTED,
                payment.getId(),
                event.getReason()
        );

        List<OutboxMessage> outboxes = messageStagingService.stage(request);

        applicationEventPublisher.publishEvent(new OutboxSavedEvent(outboxes));
    }
}
