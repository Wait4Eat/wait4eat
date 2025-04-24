package com.example.wait4eat.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Table(name = "store_sales_rank")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreSalesRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private BigDecimal totalSales;

    @Setter
    @Column(nullable = false)
    private int ranking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    @Builder
    public StoreSalesRank(Long storeId, String storeName, BigDecimal totalSales, int ranking, Dashboard dashboard) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.totalSales = totalSales;
        this.ranking = ranking;
        this.dashboard = dashboard;
    }
}
