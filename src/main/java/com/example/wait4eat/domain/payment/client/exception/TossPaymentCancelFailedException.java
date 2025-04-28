package com.example.wait4eat.domain.payment.client.exception;

import lombok.Getter;

@Getter
public class TossPaymentCancelFailedException extends RuntimeException {

    private final String code;

    public TossPaymentCancelFailedException(String code, String message) {
        super(message);
        this.code = code;
    }
}

