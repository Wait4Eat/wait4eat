package com.example.wait4eat.domain.payment.entity;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "waiting_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Waiting waiting;

    @JoinColumn(name = "coupon_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    private String paymentKey;

    @Column(nullable = false)
    private BigDecimal originalAmount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime failedAt;
    private LocalDateTime cancelledAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Payment(
            User user,
            Waiting waiting,
            Coupon coupon,
            BigDecimal originalAmount,
            BigDecimal amount,
            PaymentStatus status
    ) {
        this.user = user;
        this.waiting = waiting;
        this.coupon = coupon;
        this.originalAmount = originalAmount;
        this.amount = amount;
        this.status = status;
    }
}
