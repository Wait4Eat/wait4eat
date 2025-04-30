package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.PrePayment;
import com.example.wait4eat.domain.payment.enums.PrePaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PrePaymentRepository extends JpaRepository<PrePayment, Long> {

    @EntityGraph(attributePaths = {"user", "waiting", "coupon"})
    Optional<PrePayment> findByOrderIdAndStatus(String orderId, PrePaymentStatus status);

    Optional<PrePayment> findByOrderId(String orderId);

    List<PrePayment> findByStatusAndCreatedAtBefore(PrePaymentStatus prePaymentStatus, LocalDateTime threshold);
}
