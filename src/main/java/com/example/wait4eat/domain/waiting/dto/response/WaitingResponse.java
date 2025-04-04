package com.example.wait4eat.domain.waiting.dto.response;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WaitingResponse {
    private final Long storeId;
    private final Long userId;
    private final int peopleCount;
    private final WaitingStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime calledAt;
    private final LocalDateTime cancelledAt;
    private final LocalDateTime enteredAt;

    @Builder
    private WaitingResponse(
            Long storeId,
            Long userId,
            int peopleCount,
            WaitingStatus status,
            LocalDateTime createdAt,
            LocalDateTime calledAt,
            LocalDateTime cancelledAt,
            LocalDateTime enteredAt
    ) {
        this.storeId = storeId;
        this.userId = userId;
        this.peopleCount = peopleCount;
        this.status = status;
        this.createdAt = createdAt;
        this.calledAt = calledAt;
        this.cancelledAt = cancelledAt;
        this.enteredAt = enteredAt;
    }

    public static WaitingResponse from(Waiting waiting) {
        return WaitingResponse.builder()
                .storeId(waiting.getStore().getId())
                .userId(waiting.getUser().getId())
                .peopleCount(waiting.getPeopleCount())
                .status(waiting.getStatus())
                .createdAt(waiting.getCreatedAt())
                .calledAt(waiting.getCalledAt())
                .cancelledAt(waiting.getCancelledAt())
                .enteredAt(waiting.getEnteredAt())
                .build();
    }
}
