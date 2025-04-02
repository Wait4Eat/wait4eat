package com.example.wait4eat.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final ExceptionType exceptionType;

    public CustomException(ExceptionType exceptionType) {
        super(exceptionType.getMessage());
        this.exceptionType = exceptionType;
    }

    public CustomException(ExceptionType exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }

    public HttpStatus getHttpStatus() {
        return exceptionType.getHttpStatus();
    }
}