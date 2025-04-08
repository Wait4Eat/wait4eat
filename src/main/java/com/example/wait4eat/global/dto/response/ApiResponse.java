package com.example.wait4eat.global.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiResponse {

    private final HttpStatus status;
    @JsonProperty("success")
    private final boolean isSuccess;
    private final String message;

    protected ApiResponse(HttpStatus status, boolean isSuccess, String message) {
        this.status = status;
        this.isSuccess = isSuccess;
        this.message = message;
    }
}
