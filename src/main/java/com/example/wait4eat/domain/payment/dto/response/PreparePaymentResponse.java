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
    private final String successPath;
    private final String failPath;

    @Builder
    public PreparePaymentResponse(
            String orderId,
            BigDecimal originalAmount,
            BigDecimal amount,
            String customerKey,
            String shopName,
            String successPath,
            String failPath
    ) {
        this.orderId = orderId;
        this.originalAmount = originalAmount;
        this.amount = amount;
        this.customerKey = customerKey;
        this.shopName = shopName;
        this.successPath = successPath;
        this.failPath = failPath;
    }
}
