package com.example.wait4eat.domain.payment.dto.response;

import lombok.*;

@Getter
public class RefundPaymentResponse {
    private final String message;
    private final String refundedAt;

    @Builder
    public RefundPaymentResponse(String message, String refundedAt) {
        this.message = message;
        this.refundedAt = refundedAt;
    }
}
