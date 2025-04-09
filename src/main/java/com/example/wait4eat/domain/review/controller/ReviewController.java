package com.example.wait4eat.domain.review.controller;

import com.example.wait4eat.domain.review.dto.request.CreateReviewRequest;
import com.example.wait4eat.domain.review.dto.response.ReviewResponse;
import com.example.wait4eat.domain.review.service.ReviewService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.dto.response.PageResponse;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/api/v1/stores/reviews")
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/stores/{storeId}/reviews")
    public ResponseEntity<PageResponse<ReviewResponse>> getAllReview(
            @PathVariable Long storeId,
            @PageableDefault(page = 0, size = 10, sort = "modifiedAt", direction = Sort.Direction.DESC) Pageable pageable
            ) {
        Page<ReviewResponse> reviewPage = reviewService.getAllReview(storeId, pageable);
        return ResponseEntity.ok(PageResponse.from(reviewPage));
    }
}
