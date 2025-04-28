package com.example.wait4eat.domain.payment.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
public class TossConfirmPaymentResponse {
    private String paymentKey;
    private String orderId;
    private String status;
    private BigDecimal totalAmount;
    private String method;
    private OffsetDateTime approvedAt;
}
