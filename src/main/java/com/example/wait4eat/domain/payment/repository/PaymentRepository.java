package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Payment p
    WHERE p.status = :status AND p.paidAt BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumSalesByDate(LocalDateTime startDate, LocalDateTime endDate, PaymentStatus status);

    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Payment p
    WHERE p.waiting.store = :store AND p.createdAt BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumAmountByStoreAndCreatedAtBetween(Store store, LocalDateTime startDate, LocalDateTime endDate);
}
