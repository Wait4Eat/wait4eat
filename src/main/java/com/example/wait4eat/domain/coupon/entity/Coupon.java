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
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_event_id")
    private CouponEvent couponEvent;

    private BigDecimal discountAmount;

    @Column(nullable = false)
    private Boolean isUsed;

    @CreatedDate
    private LocalDateTime issuedAt;

    private LocalDateTime usedAt;

    @Builder
    public Coupon(
            User user,
            CouponEvent couponEvent,
            BigDecimal discountAmount
    ) {
        this.user = user;
        this.couponEvent = couponEvent;
        this.discountAmount = discountAmount;
        this.isUsed = false;
    }
}
