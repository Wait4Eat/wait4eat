package com.example.wait4eat.global.dto.response;

import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.Getter;

@Getter
public class ErrorResponse {

    private ExceptionType type;
    private String message;

    public ErrorResponse(ExceptionType type, String message) {
        this.type = type;
        this.message = message;
    }

    public static ErrorResponse from(CustomException e) {
        return new ErrorResponse(e.getExceptionType(), e.getMessage());
    }

    public static ErrorResponse of(CustomException e, String message) {
        return new ErrorResponse(e.getExceptionType(), message);
    }
}