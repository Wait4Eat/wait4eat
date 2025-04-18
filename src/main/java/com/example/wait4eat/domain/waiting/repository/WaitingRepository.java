package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long>, WaitingQueryRepository {

    Optional<Waiting> findByOrderId(String orderId);

    Optional<Waiting> findByUserIdAndStatusIn(Long userId, List<WaitingStatus> status);

    List<Waiting> findByStoreIdAndStatusOrderByActivatedAtAsc(Long storeId, WaitingStatus status);

    int countByStoreAndCreatedAtBetween(Store store, LocalDateTime start, LocalDateTime end);
}
