package com.example.wait4eat.domain.payment.event;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import lombok.Getter;

@Getter
public class PaymentRefundRequestedEvent {

    private Long waitingId;
    private String orderId;
    private String reason;

    private PaymentRefundRequestedEvent(Long waitingId, String orderId, String reason) {
        this.waitingId = waitingId;
        this.orderId = orderId;
        this.reason = reason;
    }

    public static PaymentRefundRequestedEvent of(Waiting waiting, String reason) {
        return new PaymentRefundRequestedEvent(waiting.getId(), waiting.getOrderId(), reason);
    }
}
