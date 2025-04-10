package com.example.wait4eat.domain.review.service;

import com.example.wait4eat.domain.review.dto.request.CreateReviewRequest;
import com.example.wait4eat.domain.review.dto.request.UpdateReviewRequest;
import com.example.wait4eat.domain.review.dto.response.ReviewResponse;
import com.example.wait4eat.domain.review.entity.Review;
import com.example.wait4eat.domain.review.repository.ReviewRepository;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final WaitingRepository waitingRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        Waiting findWaiting = waitingRepository.findById(request.getWaitingId()).orElseThrow(
                () -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

        if (reviewRepository.existsByWaiting(findWaiting)) {
            throw new CustomException(ExceptionType.ALREADY_REVIEW_EXISTS);
        }

        if (findWaiting.getStatus() != WaitingStatus.COMPLETED) {
            throw new CustomException(ExceptionType.WAITING_NOT_COMPLETED);
        }

        Review savedReview = reviewRepository.save(Review.builder()
                .waiting(findWaiting)
                .content(request.getContent())
                .rating(request.getRating())
                .build());

        return ReviewResponse.from(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReview(Long storeId, Pageable pageable) {
        Store findStore = storeRepository.findById(storeId).orElseThrow(
                () -> new CustomException(ExceptionType.STORE_NOT_FOUND)
        );
        return reviewRepository.getAllByStoreId(findStore.getId(), pageable)
                .map(ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllMyReview(AuthUser authUser, Pageable pageable) {
        User findUser = userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new CustomException(ExceptionType.USER_NOT_FOUND)
        );
        return reviewRepository.getAllByUserId(findUser.getId(), pageable)
                .map(ReviewResponse::from);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, AuthUser authUser, UpdateReviewRequest request) {
        Review findReview = reviewRepository.findById(reviewId).orElseThrow(
                () -> new CustomException(ExceptionType.REVIEW_NOT_FOUND)
        );
        if (!findReview.getUserId().equals(authUser.getUserId())) {
            throw new CustomException(ExceptionType.NO_PERMISSION_ACTION);
        }

        findReview.update(request);
        Review updatedReview = reviewRepository.save(findReview);
        return ReviewResponse.from(updatedReview);
    }
}
