package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("""
    SELECT SUM(p.amount)
    FROM Payment p
    WHERE p.status = :status AND p.paidAt BETWEEN :startDate AND :endDate
    """)
    Long sumSalesByDate(LocalDateTime startDate, LocalDateTime endDate, PaymentStatus paymentStatus);

    Long sumSalesByStoreAndCreatedAtBetween(Store store, LocalDateTime startDate, LocalDateTime endDate);
}
