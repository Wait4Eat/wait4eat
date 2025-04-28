package com.example.wait4eat.domain.payment.client.exception;

import lombok.Getter;

/**
 * Toss측으로부터 Error가 전달된 경우
 */

@Getter
public class TossPaymentErrorException extends RuntimeException {

    private final String code;

    public TossPaymentErrorException(String code, String message) {
        super(message);
        this.code = code;
    }
}