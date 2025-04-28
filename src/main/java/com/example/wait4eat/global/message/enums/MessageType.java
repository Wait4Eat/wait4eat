package com.example.wait4eat.global.message.enums;

public enum MessageType {
    WAITING_CALLED(true),
    COUPON_EVENT_LAUNCHED(true),
    PAYMENT_REFUND_REQUESTED(false);

    private final boolean requiresNotification;

    MessageType(boolean requiresNotification) {
        this.requiresNotification = requiresNotification;
    }

    public boolean requiresNotification() {
        return this.requiresNotification;
    }
}
