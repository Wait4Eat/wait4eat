package com.example.wait4eat.domain.waiting.dto.response;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyPastWaitingResponse {
    private final Long waitingId;
    private final Long storeId;
    private final Long userId;
    private final int peopleCount;
    private final WaitingStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime activatedAt;
    private final LocalDateTime calledAt;
    private final LocalDateTime cancelledAt;
    private final LocalDateTime enteredAt;

    @Builder
    private MyPastWaitingResponse(
            Long waitingId,
            Long storeId,
            Long userId,
            int peopleCount,
            WaitingStatus status,
            LocalDateTime createdAt,
            LocalDateTime activatedAt,
            LocalDateTime calledAt,
            LocalDateTime cancelledAt,
            LocalDateTime enteredAt
    ) {
        this.waitingId = waitingId;
        this.storeId = storeId;
        this.userId = userId;
        this.peopleCount = peopleCount;
        this.status = status;
        this.createdAt = createdAt;
        this.activatedAt = activatedAt;
        this.calledAt = calledAt;
        this.cancelledAt = cancelledAt;
        this.enteredAt = enteredAt;
    }

    public static MyPastWaitingResponse from(Waiting waiting) {
        return MyPastWaitingResponse.builder()
                .waitingId(waiting.getId())
                .storeId(waiting.getStore().getId())
                .userId(waiting.getUser().getId())
                .peopleCount(waiting.getPeopleCount())
                .status(waiting.getStatus())
                .createdAt(waiting.getCreatedAt())
                .activatedAt(waiting.getActivatedAt())
                .calledAt(waiting.getCalledAt())
                .cancelledAt(waiting.getCancelledAt())
                .enteredAt(waiting.getEnteredAt())
                .build();
    }
}
