package com.example.wait4eat.domain.dashboard.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DashboardStatsAccumulator {
    private long totalUserCount = 0;
    private long dailyUserCount = 0;
    private long totalStoreCount = 0;
    private long dailyNewStoreCount = 0;
    private BigDecimal totalDailySales = BigDecimal.ZERO;

    public void addUser(boolean isLoginYesterday) {
        this.totalUserCount++;
        if (isLoginYesterday) {
            this.dailyUserCount++;
        }
    }

    public void addStore(boolean isCreatedYesterday) {
        this.totalStoreCount++;
        if (isCreatedYesterday) {
            this.dailyNewStoreCount++;
        }
    }

    public void addPayment(BigDecimal totalDailySales) {
        this.totalDailySales = totalDailySales;
    }
}
