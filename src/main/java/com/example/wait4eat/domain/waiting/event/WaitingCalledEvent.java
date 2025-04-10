package com.example.wait4eat.domain.waiting.event;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WaitingCalledEvent {

    private final Long waitingId;
    private final Long userId;

    @Builder
    private WaitingCalledEvent(Long waitingId, Long userId) {
        this.waitingId = waitingId;
        this.userId = userId;
    }

    public static WaitingCalledEvent from(Waiting waiting) {
        return WaitingCalledEvent.builder()
                .waitingId(waiting.getId())
                .userId(waiting.getUser().getId())
                .build();
    }
}
