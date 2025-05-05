package com.example.wait4eat.domain.dashboard.dto;

import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class DashboardResponse {
    private final Long dashboardId;
    private final Long totalUserCount;
    private final Long dailyUserCount;
    private final Long totalStoreCount;
    private final Long dailyNewStoreCount;
    private final BigDecimal dailyTotalSales;
    private final LocalDate statisticsDate;
    private final List<PopularStoreResponse> popularStores;

    @Builder
    private DashboardResponse(
            Long dashboardId,
            Long totalUserCount,
            Long dailyUserCount,
            Long totalStoreCount,
            Long dailyNewStoreCount,
            BigDecimal dailyTotalSales,
            LocalDate statisticsDate,
            List<PopularStoreResponse> popularStores
    ) {
        this.dashboardId = dashboardId;
        this.totalUserCount = totalUserCount;
        this.dailyUserCount = dailyUserCount;
        this.totalStoreCount = totalStoreCount;
        this.dailyNewStoreCount = dailyNewStoreCount;
        this.dailyTotalSales = dailyTotalSales;
        this.statisticsDate = statisticsDate;
        this.popularStores = popularStores;
    }

    public static DashboardResponse from(Dashboard dashboard, List<PopularStoreResponse> popularStores) {
        return DashboardResponse.builder()
                .dashboardId(dashboard.getId())
                .totalUserCount(dashboard.getTotalUserCount())
                .dailyUserCount(dashboard.getDailyUserCount())
                .totalStoreCount(dashboard.getTotalStoreCount())
                .dailyNewStoreCount(dashboard.getDailyNewStoreCount())
                .dailyTotalSales(dashboard.getDailyTotalSales())
                .statisticsDate(dashboard.getStatisticsDate())
                .popularStores(popularStores)
                .build();
    }
}
