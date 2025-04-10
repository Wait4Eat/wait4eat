package com.example.wait4eat.domain.waiting.dto.response;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CancelWaitingResponse {
    private final LocalDateTime cancelledAt;


    @Builder
    private CancelWaitingResponse(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public static CancelWaitingResponse from(Waiting waiting) {
        return CancelWaitingResponse.builder()
                .cancelledAt(waiting.getCancelledAt())
                .build();
    }
}
