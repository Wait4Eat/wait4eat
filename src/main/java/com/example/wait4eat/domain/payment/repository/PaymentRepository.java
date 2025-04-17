package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
    boolean existsByPaymentKey(String paymentKey);
    Payment findByOrderIdAndStatus(String orderId, PaymentStatus paymentStatus);
    boolean existsByOrderId(String orderId);
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByVerifiedFalseAndCreatedAtBefore(LocalDateTime threshold);

    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Payment p
    WHERE FUNCTION('DATE', p.createdAt) = :date
      AND p.status = com.example.wait4eat.domain.payment.enums.PaymentStatus.PAID
    """)
    Long sumSalesByDate(LocalDate yesterday);
}
