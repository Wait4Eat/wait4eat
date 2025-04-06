package com.example.wait4eat.domain.waiting.dto.response;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UpdateWaitingResponse {
    private Long waitingId;
    private WaitingStatus status;
    private LocalDateTime calledAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime enteredAt;

    @Builder
    public UpdateWaitingResponse(
            Long waitingId,
            WaitingStatus status,
            LocalDateTime calledAt,
            LocalDateTime cancelledAt,
            LocalDateTime enteredAt
    ) {
        this.waitingId = waitingId;
        this.status = status;
        this.calledAt = calledAt;
        this.cancelledAt = cancelledAt;
        this.enteredAt = enteredAt;
    }

    public static UpdateWaitingResponse from(Waiting waiting) {
        return UpdateWaitingResponse.builder()
                .waitingId(waiting.getId())
                .status(waiting.getStatus())
                .calledAt(waiting.getCalledAt())
                .cancelledAt(waiting.getCancelledAt())
                .enteredAt(waiting.getEnteredAt())
                .build();
    }
}
