package com.example.wait4eat.domain.waiting.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateWaitingRequest {

    @NotNull(message = "인원수를 입력해주세요.")
    @Min(value = 1, message = "최소 1명 이상이어야 합니다.")
    private int peopleCount;

    @Builder
    private CreateWaitingRequest(int peopleCount) {
        this.peopleCount = peopleCount;
    }

}
