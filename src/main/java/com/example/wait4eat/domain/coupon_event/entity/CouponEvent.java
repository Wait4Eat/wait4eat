package com.example.wait4eat.domain.coupon_event.entity;

import com.example.wait4eat.domain.store.entity.Store;
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
@Table(name = "coupon_events")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    private String name;

    private BigDecimal discountAmount;

    private int totalQuantity;

    private int issuedQuantity;

    private LocalDateTime expiresAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public CouponEvent(
            Store store,
            String name,
            BigDecimal discountAmount,
            int totalQuantity,
            int issuedQuantity,
            LocalDateTime expiresAt
    ) {
        this.store = store;
        this.name = name;
        this.discountAmount = discountAmount;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = issuedQuantity;
        this.expiresAt = expiresAt;
    }
}
