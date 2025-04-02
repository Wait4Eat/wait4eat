package com.example.wait4eat.domain.payment.entity;


import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

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
