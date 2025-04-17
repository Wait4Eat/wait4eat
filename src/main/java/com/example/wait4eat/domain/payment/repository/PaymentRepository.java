package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Payment p
    WHERE FUNCTION('DATE', p.createdAt) = :date
      AND p.status = com.example.wait4eat.domain.payment.enums.PaymentStatus.PAID
    """)
    Long sumSalesByDate(LocalDate yesterday);
}
