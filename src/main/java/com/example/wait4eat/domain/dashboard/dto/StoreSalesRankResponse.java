package com.example.wait4eat.domain.dashboard.dto;

import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class StoreSalesRankResponse {
    private final Long storeId;
    private final String storeName;
    private final BigDecimal totalSales;
    private final int ranking;
    private final Long dashboardId;
    private final LocalDate statisticsDate;

    @Builder
    private StoreSalesRankResponse(Long storeId, String storeName, BigDecimal totalSales, int ranking, Long dashboardId, LocalDate statisticsDate) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.totalSales = totalSales;
        this.ranking = ranking;
        this.dashboardId = dashboardId;
        this.statisticsDate = statisticsDate;
    }

    public static StoreSalesRankResponse from(StoreSalesRank salesRank) {
        return StoreSalesRankResponse.builder()
                .storeId(salesRank.getStoreId())
                .storeName(salesRank.getStoreName())
                .totalSales(salesRank.getTotalSales())
                .ranking(salesRank.getRanking())
                .dashboardId(salesRank.getDashboard().getId())
                .statisticsDate(salesRank.getDashboard().getStatisticsDate())
                .build();
    }
}
