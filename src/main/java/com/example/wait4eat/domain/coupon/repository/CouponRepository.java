package com.example.wait4eat.domain.coupon.repository;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import com.example.wait4eat.domain.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByUserIdAndCouponEventId(Long userId, Long couponEventId);

    Page<Coupon> findAllByUser(User user, Pageable pageable);

    Coupon findByUserIdAndCouponEventId(Long userId, Long couponEventId);
}
