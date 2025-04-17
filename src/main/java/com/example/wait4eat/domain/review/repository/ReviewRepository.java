package com.example.wait4eat.domain.review.repository;

import com.example.wait4eat.domain.review.entity.Review;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByWaiting(Waiting waiting);

    @Query("SELECT r FROM Review r JOIN r.waiting w WHERE w.store.id = :storeId")
    Page<Review> getAllByStoreId(Long storeId, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN r.waiting w WHERE w.user.id = :userId")
    Page<Review> getAllByUserId(Long userId, Pageable pageable);
}
