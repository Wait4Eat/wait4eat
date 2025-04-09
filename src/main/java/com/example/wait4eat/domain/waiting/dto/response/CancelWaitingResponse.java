package com.example.wait4eat.domain.waiting.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CancelWaitingResponse {
    private final String message;

    @Builder
    private CancelWaitingResponse(String message) {
        this.message = message;
    }

    public static CancelWaitingResponse from(String message) {
        return CancelWaitingResponse.builder()
                .message(message)
                .build();
    }
}
