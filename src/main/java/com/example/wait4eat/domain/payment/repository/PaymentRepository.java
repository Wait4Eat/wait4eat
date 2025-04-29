package com.example.wait4eat.domain.payment.repository;

import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
    boolean existsByPaymentKey(String paymentKey);
    Payment findByOrderIdAndStatus(String orderId, PaymentStatus paymentStatus);
    boolean existsByOrderId(String orderId);
    Optional<Payment> findByOrderId(String orderId);
}
