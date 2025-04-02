package com.example.wait4eat.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionType {

    // Validation / Request Errors
    REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청값 검증에 실패했습니다."),
    REQUEST_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다. "),

    // Auth
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "로그인 정보가 올바르지 않습니다."),
    INCORRECT_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // Forbidden
    NO_PERMISSION_ACTION(HttpStatus.FORBIDDEN, "권한이 없는 작업입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 사용자를 찾을 수 없습니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 가게를 찾을 수 없습니다."),

    // Coupon
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 쿠폰을 찾을 수 없습니다."),

    // Coupon Event
    COUPON_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 쿠폰 이벤트를 찾을 수 없습니다."),

    // Waiting
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 웨이팅을 찾을 수 없습니다."),

    // Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ExceptionType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
