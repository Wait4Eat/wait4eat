package com.example.wait4eat.domain.payment.event;

import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import lombok.Getter;

@Getter
public class PaymentConfirmedEvent {

    private final Long paymentId;
    private final Long waitingId;

    public PaymentConfirmedEvent(Long paymentId, Long waitingId) {
        this.paymentId = paymentId;
        this.waitingId = waitingId;
    }

    public static PaymentConfirmedEvent of(Payment payment, Waiting waiting) {
        return new PaymentConfirmedEvent(payment.getId(), waiting.getId());
    }
}
