package com.example.wait4eat.domain.dashboard.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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
    private int totalUserCount;

    @Column(nullable = false)
    private int dailyUserCount;

    @Column(nullable = false)
    private int totalStoreCount;

    @Column(nullable = false)
    private int dailyNewStoreCount;

    @Column(nullable = false)
    private int dailyTotalSales;

    @Column(nullable = false, unique = true)
    private LocalDate statisticsDate;
}
