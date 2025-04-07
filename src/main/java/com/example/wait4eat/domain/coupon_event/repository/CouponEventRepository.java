package com.example.wait4eat.domain.coupon_event.repository;

import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
    boolean existsByStoreId(Long storeId);
}
