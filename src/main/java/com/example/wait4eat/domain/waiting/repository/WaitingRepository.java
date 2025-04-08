package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long>, WaitingQueryRepository {
    Optional<Waiting> findByOrderId(String orderId);
    int countByStoreIdAndStatus(Long storeId, WaitingStatus status);
    Optional<Waiting> findByUserIdAndStatus(Long userId, WaitingStatus status);
    List<Waiting> findByStoreIdAndStatusOrderByCreatedAtAsc(Long storeId, WaitingStatus status);


}
