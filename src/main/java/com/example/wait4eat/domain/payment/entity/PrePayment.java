package com.example.wait4eat.domain.payment.entity;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.payment.enums.PrePaymentStatus;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pre_payments")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "waiting_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Waiting waiting;

    @JoinColumn(name = "coupon_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    @Column(nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrePaymentStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Column(nullable = false)
    private BigDecimal originalAmount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Builder
    public PrePayment(
            User user,
            Waiting waiting,
            Coupon coupon,
            String orderId,
            BigDecimal originalAmount,
            BigDecimal amount,
            PrePaymentStatus status
    ) {
        this.user = user;
        this.waiting = waiting;
        this.coupon = coupon;
        this.orderId = orderId;
        this.originalAmount = originalAmount;
        this.amount = amount;
        this.status = status;
    }

    public void markAsCompleted() {
        this.status = PrePaymentStatus.COMPLETED;
    }

    public void markAsExpired() {
        this.status = PrePaymentStatus.EXPIRED;
    }

    public void markAsFailed() {
        this.status = PrePaymentStatus.FAILED;
    }
}
