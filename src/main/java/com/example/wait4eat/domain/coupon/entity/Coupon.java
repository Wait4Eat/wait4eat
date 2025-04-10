package com.example.wait4eat.domain.coupon.entity;

import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import com.example.wait4eat.domain.user.entity.User;
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
@Table(name = "coupons")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_event_id", nullable = false)
    private CouponEvent couponEvent;

    @Column(nullable = false)
    private BigDecimal discountAmount;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime issuedAt;

    @Column(nullable = false)
    private Boolean isUsed;

    private LocalDateTime usedAt;

    @Builder
    public Coupon(
            User user,
            CouponEvent couponEvent,
            BigDecimal discountAmount,
            LocalDateTime expiresAt,
            LocalDateTime issuedAt,
            Boolean isUsed,
            LocalDateTime usedAt
    ) {
        this.user = user;
        this.couponEvent = couponEvent;
        this.discountAmount = discountAmount;
        this.expiresAt = expiresAt;
        this.issuedAt = issuedAt;
        this.isUsed = false;
        this.usedAt = usedAt;
    }
}
