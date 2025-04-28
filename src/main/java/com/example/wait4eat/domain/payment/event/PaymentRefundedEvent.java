package com.example.wait4eat.domain.payment.event;

import com.example.wait4eat.domain.payment.entity.Payment;
import lombok.Getter;

@Getter
public class PaymentRefundedEvent {

    private final Long paymentId;
    private final Long waitingId;

    private PaymentRefundedEvent(Long paymentId, Long waitingId) {
        this.paymentId = paymentId;
        this.waitingId = waitingId;
    }

    public static PaymentRefundedEvent from(Payment payment) {
        return new PaymentRefundedEvent(payment.getId(), payment.getWaiting().getId());
    }
}
