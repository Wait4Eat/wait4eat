package com.example.wait4eat.global.dto.response;

import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.Getter;

@Getter
public class ErrorResponse extends ApiResponse {

    private final ExceptionType type;

    public ErrorResponse(ExceptionType type, String message) {
        super(type.getHttpStatus(), false, message);
        this.type = type;
    }

    public static ErrorResponse from(CustomException e) {
        return new ErrorResponse(e.getExceptionType(), e.getMessage());
    }

    public static ErrorResponse of(CustomException e, String message) {
        return new ErrorResponse(e.getExceptionType(), message);
    }
}