package com.example.wait4eat.domain.dashboard.service;

import com.example.wait4eat.domain.dashboard.dto.DashboardResponse;
import com.example.wait4eat.domain.dashboard.dto.PopularStoreResponse;
import com.example.wait4eat.domain.dashboard.dto.StoreSalesRankResponse;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.dashboard.repository.DashboardRepository;
import com.example.wait4eat.domain.dashboard.repository.PopularStoreRepository;
import com.example.wait4eat.domain.dashboard.repository.StoreSalesRankRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final DashboardRepository dashboardRepository;
    private final PopularStoreRepository popularStoreRepository;
    private final StoreSalesRankRepository storeSalesRankRepository;

    @Transactional(readOnly = true)
    public Page<DashboardResponse> getAllDashboard(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new CustomException(ExceptionType.INVALID_DATE_RANGE);
        }

        Page<Dashboard> dashboards;

        if (startDate != null && endDate != null && startDate.isBefore(endDate)) {
            dashboards = dashboardRepository.findByStatisticsDateBetween(startDate, endDate, pageable);
        } else {
            // 날짜가 제공되지 않으면, 전체 대시보드 조회
            dashboards = dashboardRepository.findAll(pageable);
        }

        return dashboards.map(dashboard -> {
            List<PopularStore> popularStores = popularStoreRepository
                    .findByDashboardIdOrderByRanking(dashboard.getId());

            List<PopularStoreResponse> popularStoreResponses = popularStores.stream()
                    .map(store -> PopularStoreResponse.builder()
                            .storeId(store.getStoreId())
                            .storeName(store.getStoreName())
                            .waitingCount(store.getWaitingCount())
                            .ranking(store.getRanking())
                            .build())
                    .toList();

            return DashboardResponse.from(dashboard, popularStoreResponses);
        });
    }

    @Transactional(readOnly = true)
    public Page<StoreSalesRankResponse> getStoreSalesRanks(LocalDate targetDate, Pageable pageable) {
        Dashboard findDashboard = dashboardRepository.findByStatisticsDateOrElseThrow(targetDate);
        Page<StoreSalesRank> storeSalesRanks = storeSalesRankRepository.findByDashboardOrderByRanking(findDashboard, pageable);
        return storeSalesRanks.map(StoreSalesRankResponse::from);
    }
}
