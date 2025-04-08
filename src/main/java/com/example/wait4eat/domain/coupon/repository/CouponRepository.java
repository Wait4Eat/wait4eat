package com.example.wait4eat.domain.coupon.repository;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByUserIdAndCouponEventId(Long userId, Long couponEventId);

    Page<Coupon> findAllByUser(User user, Pageable pageable);

    Coupon findByUserIdAndCouponEventId(Long userId, Long couponEventId);
}
