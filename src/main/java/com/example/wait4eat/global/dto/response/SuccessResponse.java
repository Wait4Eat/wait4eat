package com.example.wait4eat.global.dto.response;

import com.example.wait4eat.global.dto.consts.ApiMessage;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SuccessResponse<T> extends ApiResponse {

    private final T data;

    private SuccessResponse(T data, String message) {
        super(HttpStatus.OK, true, message);
        this.data = data;
    }

    // 데이터만 넣고 싶을 경우
    public static <T> SuccessResponse<T> from(T data) {
        return new SuccessResponse<>(data, ApiMessage.DEFAULT_SUCCESS_MESSAGE);
    }

    // 메세지만 넣고 싶을 경우
    public static <T> SuccessResponse<T> from(String message) {
        return new SuccessResponse<>(null, message);
    }

    // 데이터 + 메세지를 함께 반환하는 경우
    public static <T> SuccessResponse<T> of(T data, String message) {
        return new SuccessResponse<>(data, message);
    }
}