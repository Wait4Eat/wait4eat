package com.example.wait4eat.domain.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
public class SuccessPaymentResponse {
    private final String message;
    private final Long waitingId;
    private final Long paymentId;
    private final BigDecimal amount;

    @Builder
    public SuccessPaymentResponse(String message, Long waitingId, Long paymentId, BigDecimal amount) {
        this.message = message;
        this.waitingId = waitingId;
        this.paymentId = paymentId;
        this.amount = amount;
    }
}
