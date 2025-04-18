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
    INVALID_SCOPE(HttpStatus.UNAUTHORIZED, "유효하지 않은 scope의 토큰입니다."),
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
    NOT_OWNER_OF_STORE(HttpStatus.FORBIDDEN, "해당 가게 사장님만 가능한 작업입니다."),
    STORE_NOT_MATCH_USER(HttpStatus.BAD_REQUEST,  "해당 유저의 가게가 아닙니다."),
    STORE_ALREADY_EXISTS(HttpStatus.CONFLICT, "사장님은 하나의 가게만 등록할 수 있습니다."),

    // Store Image
    STORE_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가게 이미지를 찾을 수 없습니다."),
    STORE_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "가게당 최대 5개의 이미지만 업로드할 수 있습니다."),
    STORE_IMAGE_NOT_BELONGS_TO_STORE(HttpStatus.BAD_REQUEST, "해당 가게의 이미지가 아닙니다."),

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
    INVALID_WAITING_STATUS_UPDATE(HttpStatus.BAD_REQUEST, "유효하지 않은 웨이팅 상태 변경입니다."),
    UNAUTHORIZED_CANCEL_WAITING(HttpStatus.FORBIDDEN, "웨이팅 취소 권한이 없습니다."),
    ALREADY_FINISHED_WAITING(HttpStatus.BAD_REQUEST, "이미 완료 또는 취소된 웨이팅입니다."),
    SINGLE_WAIT_ALLOWED(HttpStatus.BAD_REQUEST, "한 번에 하나의 웨이팅만 허용됩니다."),
    NO_PERMISSION_TO_ACCESS_STORE_WAITING(HttpStatus.FORBIDDEN, "해당 가게 웨이팅 조회 권한이 없습니다"),

    // StoreWishlist
    STORE_WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 위시리스트를 찾을 수 없습니다."),
    ALREADY_WISHLIST_EXISTS(HttpStatus.CONFLICT, "이미 찜한 가게입니다."),

    // Review
    WAITING_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "아직 웨이팅이 완료되지 않았습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND,  "해당 리뷰를 찾을 수 없습니다."),
    ALREADY_REVIEW_EXISTS(HttpStatus.CONFLICT, "이미 리뷰를 작성하였습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 알림을 찾을 수 없습니다."),

    // File
    FILE_LIST_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 파일을 1개 이상 첨부해주세요."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

    // Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ExceptionType(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
