package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Long sumSalesByDate(LocalDate yesterday);
}
