package com.example.wait4eat.domain.waiting.dto.response;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateWaitingResponse {
    private final Long waitingId;
    private final Long storeId;
    private final Long userId;
    private final String orderId;
    private final int peopleCount;
    private final int myWaitingOrder;
    private final WaitingStatus status;
    private final LocalDateTime createdAt;

    @Builder
    private CreateWaitingResponse(
            Long waitingId,
            Long storeId,
            Long userId,
            String orderId,
            int peopleCount,
            int myWaitingOrder,
            WaitingStatus status,
            LocalDateTime createdAt
    ) {
        this.waitingId = waitingId;
        this.storeId = storeId;
        this.userId = userId;
        this.orderId = orderId;
        this.peopleCount = peopleCount;
        this.myWaitingOrder = myWaitingOrder;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static CreateWaitingResponse from(Waiting waiting) {
        return CreateWaitingResponse.builder()
                .waitingId(waiting.getId())
                .storeId(waiting.getStore().getId())
                .userId(waiting.getUser().getId())
                .orderId(waiting.getOrderId())
                .peopleCount(waiting.getPeopleCount())
                .myWaitingOrder(waiting.getMyWaitingOrder())
                .status(waiting.getStatus())
                .createdAt(waiting.getCreatedAt())
                .build();
    }
}
