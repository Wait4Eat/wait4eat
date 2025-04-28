package com.example.wait4eat.global.message.service;

import com.example.wait4eat.domain.payment.service.PaymentService;
import com.example.wait4eat.global.message.outbox.enums.AggregateType;
import com.example.wait4eat.global.message.payload.EventMessagePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentMessageProcessor {

    private final PaymentService paymentService;

    public void handlePaymentMessage(EventMessagePayload payload) {
        if (AggregateType.PAYMENT_REFUND.name().equals(payload.getAggregateType())) {
            paymentService.refundPayment(payload.getTargetId(), payload.getMessage());
        }
    }
}
