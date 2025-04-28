package com.example.wait4eat.domain.payment.dto.response;

import com.example.wait4eat.domain.payment.entity.Payment;
import lombok.*;

import java.math.BigDecimal;

@Getter
public class SuccessPaymentResponse {
    private final Long waitingId;
    private final Long paymentId;
    private final BigDecimal amount;

    @Builder
    private SuccessPaymentResponse(Long waitingId, Long paymentId, BigDecimal amount) {
        this.waitingId = waitingId;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public static SuccessPaymentResponse from(Payment payment) {
        return SuccessPaymentResponse.builder()
                .paymentId(payment.getId())
                .waitingId(payment.getWaiting().getId())
                .amount(payment.getAmount())
                .build();
    }
}
