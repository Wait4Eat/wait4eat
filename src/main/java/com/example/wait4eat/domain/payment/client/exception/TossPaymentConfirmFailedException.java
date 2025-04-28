package com.example.wait4eat.domain.payment.client.exception;

import lombok.Getter;

@Getter
public class TossPaymentConfirmFailedException extends RuntimeException {

    private final String code;

    public TossPaymentConfirmFailedException(String code, String message) {
        super(message);
        this.code = code;
    }
}
