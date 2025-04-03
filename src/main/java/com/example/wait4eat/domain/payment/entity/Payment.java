package com.example.wait4eat.domain.payment.entity;


import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiting_id")
    private Waiting waiting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    private String orderId;

    private String paymentKey;

    private BigDecimal originalAmount;

    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime failedAt;

    private LocalDateTime cancelledAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Payment(
            User user,
            Waiting waiting,
            Coupon coupon,
            String orderId,
            BigDecimal originalAmount,
            BigDecimal finalAmount,
            PaymentStatus status
    ) {
        this.user = user;
        this.waiting = waiting;
        this.coupon = coupon;
        this.orderId = orderId;
        this.originalAmount = originalAmount;
        this.finalAmount = finalAmount;
        this.status = status;
    }
}
