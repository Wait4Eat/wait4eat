package com.example.wait4eat.domain.review.repository;

import com.example.wait4eat.domain.review.entity.Review;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByWaiting(Waiting waiting);
}
