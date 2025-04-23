package com.example.wait4eat.domain.coupon_event.repository;

import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
    boolean existsByStoreId(Long storeId);

    // 비관적 락 추가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ce from CouponEvent ce where ce.id = :couponEventId")
    Optional<CouponEvent> findByIdWithPessimisticLock(Long couponEventId);
}
