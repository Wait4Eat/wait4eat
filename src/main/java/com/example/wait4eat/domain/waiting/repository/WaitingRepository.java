package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long>, WaitingQueryRepository {

    Optional<Waiting> findByOrderId(String orderId);

    Optional<Waiting> findByUserIdAndStatusIn(Long userId, List<WaitingStatus> status);

    List<Waiting> findByStoreIdAndStatusOrderByActivatedAtAsc(Long storeId, WaitingStatus status);

    // 비관적 락을 위한 메서드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Waiting w where w.id = :id")
    Optional<Waiting> findByIdWithPessimisticLock(@Param("id") Long id);
}
