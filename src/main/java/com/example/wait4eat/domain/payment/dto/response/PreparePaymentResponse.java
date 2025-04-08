package com.example.wait4eat.domain.payment.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
public class PreparePaymentResponse {

    private final String orderId;
    private final BigDecimal originalAmount;
    private final BigDecimal amount;
    private final String customerKey;
    private final String shopName;
    private final String successUrl;
    private final String failUrl;

    @Builder
    public PreparePaymentResponse(
            String orderId,
            BigDecimal originalAmount,
            BigDecimal amount,
            String customerKey,
            String shopName,
            String successUrl,
            String failUrl
    ) {
        this.orderId = orderId;
        this.originalAmount = originalAmount;
        this.amount = amount;
        this.customerKey = customerKey;
        this.shopName = shopName;
        this.successUrl = successUrl;
        this.failUrl = failUrl;
    }
}
