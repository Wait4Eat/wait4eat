package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
