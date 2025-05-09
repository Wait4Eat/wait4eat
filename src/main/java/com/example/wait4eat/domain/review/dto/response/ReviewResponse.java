package com.example.wait4eat.domain.review.dto.response;

import com.example.wait4eat.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewResponse {
    private final Long id;
    private final Long userId;
    private final Long storeId;
    private final String content;
    private final double rating;
    private final boolean isBlinded;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    @Builder
    private ReviewResponse(
            Long id,
            Long userId,
            Long storeId,
            String content,
            double rating,
            boolean isBlinded,
            LocalDateTime createdAt,
            LocalDateTime modifiedAt
    ) {
        this.id = id;
        this.userId = userId;
        this.storeId = storeId;
        this.content = content;
        this.rating = rating;
        this.isBlinded = isBlinded;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static ReviewResponse from(Review review) {
        if (review.isBlinded()) {
            return ReviewResponse.builder()
                    .id(review.getId())
                    .userId(review.getUserId())
                    .storeId(review.getStoreId())
                    .content("블라인드 처리된 글입니다.")
                    .rating(review.getRating())
                    .isBlinded(review.isBlinded())
                    .createdAt(review.getCreatedAt())
                    .modifiedAt(review.getModifiedAt())
                    .build();
        } else {
            return ReviewResponse.builder()
                    .id(review.getId())
                    .userId(review.getUserId())
                    .storeId(review.getStoreId())
                    .content(review.getContent())
                    .rating(review.getRating())
                    .isBlinded(review.isBlinded())
                    .createdAt(review.getCreatedAt())
                    .modifiedAt(review.getModifiedAt())
                    .build();
        }

    }
}
