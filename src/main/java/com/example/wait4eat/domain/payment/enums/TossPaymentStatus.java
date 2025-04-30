package com.example.wait4eat.domain.payment.enums;

import lombok.Getter;

@Getter
public enum TossPaymentStatus {
    DONE,
    ABORTED,
    EXPIRED,
    CANCELED;

    public static TossPaymentStatus from(String status) {
        try {
            return TossPaymentStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown Webhook Status: " + status);
        }
    }
}
