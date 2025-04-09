package com.example.wait4eat.domain.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdateReviewRequest {

    @NotBlank(message = "내용 입력은 필수입니다.")
    private String content;

    @NotNull(message = "점수 입력은 필수입니다.")
    private double rating;
}
