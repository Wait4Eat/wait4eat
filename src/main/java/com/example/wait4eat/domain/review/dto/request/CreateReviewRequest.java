package com.example.wait4eat.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateReviewRequest {

    @NotNull
    private Long waitingId;

    @NotBlank(message = "내용 입력은 필수입니다.")
    private String content;

    @NotNull(message = "점수 입력은 필수입니다.")
    private double rating;
}
