package com.example.wait4eat.domain.waiting.dto.response;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CancelWaitingResponse {
    private final String message;

    @Builder
    private CancelWaitingResponse(String message) {
        this.message = message;
    }

    public static CancelWaitingResponse from(Waiting waiting) {
        return CancelWaitingResponse.builder()
                .message("웨이팅이 성공적으로 취소되었습니다.")
                .build();
    }
}
