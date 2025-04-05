package com.example.wait4eat.domain.waiting.dto.response;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MyWaitingResponse {
    private final Long waitingId;
    private final Long storeId;
    private final Long userId;
    private final int peopleCount;
    private final String status;
    private final int waitingTeamCount;
    private final int myWaitingOrder;
    private final LocalDateTime createdAt;
    private final LocalDateTime calledAt;
    private final LocalDateTime cancelledAt;
    private final LocalDateTime enteredAt;

    @Builder
    private MyWaitingResponse(
            Long waitingId,
            Long storeId,
            Long userId,
            int peopleCount,
            String status,
            int waitingTeamCount,
            int myWaitingOrder,
            LocalDateTime createdAt,
            LocalDateTime calledAt,
            LocalDateTime cancelledAt,
            LocalDateTime enteredAt
    ) {
        this.waitingId = waitingId;
        this.storeId = storeId;
        this.userId = userId;
        this.peopleCount = peopleCount;
        this.status = status;
        this.waitingTeamCount = waitingTeamCount;
        this.myWaitingOrder = myWaitingOrder;
        this.createdAt = createdAt;
        this.calledAt = calledAt;
        this.cancelledAt = cancelledAt;
        this.enteredAt = enteredAt;
    }

    public static MyWaitingResponse from(Waiting waiting) {
        return MyWaitingResponse.builder()
                .waitingId(waiting.getId())
                .storeId(waiting.getStore().getId())
                .userId(waiting.getUser().getId())
                .peopleCount(waiting.getPeopleCount())
                .status(waiting.getStatus().name())
                .waitingTeamCount(waiting.getWaitingTeamCount())
                .myWaitingOrder(waiting.getMyWaitingOrder())
                .createdAt(waiting.getCreatedAt())
                .calledAt(waiting.getCalledAt())
                .cancelledAt(waiting.getCancelledAt())
                .enteredAt(waiting.getEnteredAt())
                .build();
    }
}
