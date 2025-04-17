package com.example.wait4eat.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "dashboards")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dashboard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long totalUserCount;

    @Column(nullable = false)
    private Long dailyUserCount;

    @Column(nullable = false)
    private Long totalStoreCount;

    @Column(nullable = false)
    private Long dailyNewStoreCount;

    @Column(nullable = false)
    private Long dailyTotalSales;

    @Column(nullable = false, unique = true)
    private LocalDate statisticsDate;

    @Builder
    public Dashboard(
            Long totalUserCount,
            Long dailyUserCount,
            Long totalStoreCount,
            Long dailyNewStoreCount,
            Long dailyTotalSales,
            LocalDate statisticsDate
    ) {
        this.totalUserCount = totalUserCount;
        this.dailyUserCount = dailyUserCount;
        this.totalStoreCount = totalStoreCount;
        this.dailyNewStoreCount = dailyNewStoreCount;
        this.dailyTotalSales = dailyTotalSales;
        this.statisticsDate = statisticsDate;
    }
}
