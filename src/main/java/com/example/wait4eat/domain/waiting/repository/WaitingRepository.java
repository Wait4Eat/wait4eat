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

    // 웨이팅 존재 여부만 체크
    boolean existsByUserIdAndStatusIn(Long userId, List<WaitingStatus> statuses);

    // 비관적 락을 위한 메서드
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Waiting w where w.id = :id")
    Optional<Waiting> findByIdForUpdate(@Param("id") Long id);

    // 레디스 성능 테스트를 위한 메서드
    List<Waiting> findByStoreIdAndStatus(Long storeId, WaitingStatus waitingStatus);
}
