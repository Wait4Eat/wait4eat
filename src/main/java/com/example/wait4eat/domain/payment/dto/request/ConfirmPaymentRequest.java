package com.example.wait4eat.domain.payment.dto.request;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ConfirmPaymentRequest {
    private String paymentKey;
    private String orderId;
    private BigDecimal amount;
}
