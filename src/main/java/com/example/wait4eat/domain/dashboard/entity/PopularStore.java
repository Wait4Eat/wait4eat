package com.example.wait4eat.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "popular_stores")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private String storeName;

    @Column(nullable = false)
    private Long waitingCount;

    @Column(nullable = false)
    private int ranking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dashboard_id", nullable = false)
    private Dashboard dashboard;

    @Builder
    public PopularStore(
            Long storeId,
            String storeName,
            Long waitingCount,
            int ranking,
            Dashboard dashboard
    ) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.waitingCount = waitingCount;
        this.ranking = ranking;
        this.dashboard = dashboard;
    }
}
