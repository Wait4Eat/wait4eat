package com.example.wait4eat.domain.waiting.dto.request;

import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateWaitingRequest {

    @NotNull(message = "웨이팅 상태를 선택해주세요.")
    private WaitingStatus status;

    @Builder
    private UpdateWaitingRequest(
            WaitingStatus status
    ) {
        this.status = status;
    }
}
