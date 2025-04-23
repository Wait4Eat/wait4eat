package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
    boolean existsByPaymentKey(String paymentKey);
    Payment findByOrderIdAndStatus(String orderId, PaymentStatus paymentStatus);
    boolean existsByOrderId(String orderId);
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByVerifiedFalseAndCreatedAtBefore(LocalDateTime threshold);

    @Query("""
    SELECT SUM(p.amount)
    FROM Payment p
    WHERE p.status = :status AND p.paidAt BETWEEN :startDate AND :endDate
    """)
    Long sumSalesByDate(LocalDateTime startDate, LocalDateTime endDate, PaymentStatus paymentStatus);

    Long sumSalesByStoreAndCreatedAtBetween(Store store, LocalDateTime startDate, LocalDateTime endDate);
}
