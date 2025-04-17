package com.example.wait4eat.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreateReviewRequest {

    @NotNull
    private Long waitingId;

    @NotBlank(message = "내용 입력은 필수입니다.")
    private String content;

    @NotNull(message = "점수 입력은 필수입니다.")
    @Max(5) @Min(1)
    private double rating;
}
