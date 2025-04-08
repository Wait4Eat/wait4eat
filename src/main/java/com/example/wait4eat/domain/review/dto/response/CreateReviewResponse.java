package com.example.wait4eat.domain.review.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateReviewResponse {
    private final Long id;
    private final Long userId;
    private final Long storeId;
    private final String content;
    private final double rating;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    @Builder
    private CreateReviewResponse(Long id, Long userId, Long storeId, String content, double rating, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.userId = userId;
        this.storeId = storeId;
        this.content = content;
        this.rating = rating;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
