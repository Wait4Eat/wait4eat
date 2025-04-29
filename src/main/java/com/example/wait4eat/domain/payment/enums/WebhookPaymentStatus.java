package com.example.wait4eat.domain.payment.enums;

import lombok.Getter;

@Getter
public enum WebhookPaymentStatus {
    DONE,
    ABORTED,
    EXPIRED,
    CANCELED;

    public static WebhookPaymentStatus from(String status) {
        try {
            return WebhookPaymentStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown Webhook Status: " + status);
        }
    }
}
