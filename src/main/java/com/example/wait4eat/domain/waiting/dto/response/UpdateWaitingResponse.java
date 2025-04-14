package com.example.wait4eat.domain.waiting.dto.response;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateWaitingResponse {
    private final Long waitingId;
    private final WaitingStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime calledAt;
    private final LocalDateTime cancelledAt;
    private final LocalDateTime enteredAt;

    @Builder
    private UpdateWaitingResponse(
            Long waitingId,
            WaitingStatus status,
            LocalDateTime createdAt,
            LocalDateTime calledAt,
            LocalDateTime cancelledAt,
            LocalDateTime enteredAt
    ) {
        this.waitingId = waitingId;
        this.status = status;
        this.createdAt = createdAt;
        this.calledAt = calledAt;
        this.cancelledAt = cancelledAt;
        this.enteredAt = enteredAt;
    }

    public static UpdateWaitingResponse from(Waiting waiting) {
        return UpdateWaitingResponse.builder()
                .waitingId(waiting.getId())
                .status(waiting.getStatus())
                .createdAt(waiting.getCreatedAt())
                .calledAt(waiting.getCalledAt())
                .cancelledAt(waiting.getCancelledAt())
                .enteredAt(waiting.getEnteredAt())
                .build();
    }
}
