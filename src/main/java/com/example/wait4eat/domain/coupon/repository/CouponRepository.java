package com.example.wait4eat.domain.coupon.repository;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}