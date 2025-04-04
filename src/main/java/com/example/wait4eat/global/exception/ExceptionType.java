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

    // Jwt
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.BAD_REQUEST, "지원되지 않는 JWT 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),

    // Forbidden
    NO_PERMISSION_ACTION(HttpStatus.FORBIDDEN, "권한이 없는 작업입니다."),
    INVALID_USER_ROLE(HttpStatus.FORBIDDEN, "유효하지 않은 권한입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),

    // Store
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 가게를 찾을 수 없습니다."),

    // Coupon
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 쿠폰을 찾을 수 없습니다."),
    COUPON_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,  "이미 발급받은 쿠폰입니다."),
    COUPON_SOLD_OUT(HttpStatus.BAD_REQUEST,  "선착순 쿠폰이 소진되었습니다."),

    // Coupon Event
    COUPON_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 쿠폰 이벤트를 찾을 수 없습니다."),
    COUPON_EVENT_NOT_MATCH_STORE(HttpStatus.BAD_REQUEST, "해당 가게의 쿠폰이 아닙니다."),
    COUPON_EVENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 진행 중인 쿠폰 이벤트가 있습니다."),

    // Waiting
    WAITING_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 웨이팅을 찾을 수 없습니다."),
    INVALID_WAITING_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 웨이팅 타입입니다."),

    // Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ExceptionType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
