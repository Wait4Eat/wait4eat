package com.example.wait4eat.domain.payment.dto.response;

import com.example.wait4eat.domain.payment.entity.PrePayment;
import lombok.*;

import java.math.BigDecimal;

@Getter
public class PreparePaymentResponse {

    private final String orderId;
    private final BigDecimal originalAmount;
    private final BigDecimal amount;
    private final String shopName;

    @Builder
    private PreparePaymentResponse(
            String orderId,
            BigDecimal originalAmount,
            BigDecimal amount,
            String shopName
    ) {
        this.orderId = orderId;
        this.originalAmount = originalAmount;
        this.amount = amount;
        this.shopName = shopName;
    }

    public static PreparePaymentResponse from(PrePayment prePayment) {
        return PreparePaymentResponse.builder()
                .orderId(prePayment.getOrderId())
                .originalAmount(prePayment.getOriginalAmount())
                .amount(prePayment.getAmount())
                .shopName(prePayment.getWaiting().getStore().getName())
                .build();
    }
}
