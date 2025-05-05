package com.example.wait4eat.domain.dashboard.controller;

import com.example.wait4eat.domain.dashboard.dto.DashboardResponse;
import com.example.wait4eat.domain.dashboard.dto.StoreSalesRankResponse;
import com.example.wait4eat.domain.dashboard.service.DashboardService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @Secured(UserRole.Authority.ADMIN)
    @GetMapping("/api/v1/dashboards")
    public ResponseEntity<PageResponse<DashboardResponse>> getDashboards(
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate,
            @PageableDefault(page = 0, size = 10, sort = "statisticsDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<DashboardResponse> allDashboard = dashboardService.getAllDashboard(startDate, endDate, pageable);
        return ResponseEntity.ok(PageResponse.from(allDashboard));
    }

    @Secured(UserRole.Authority.ADMIN)
    @GetMapping("/api/v1/salesranks")
    public ResponseEntity<PageResponse<StoreSalesRankResponse>> getSalesRanks(
            @RequestParam(name = "targetDate") LocalDate targetDate,
            @PageableDefault(page = 0, size = 10, sort = "ranking") Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponse.from(dashboardService.getStoreSalesRanks(targetDate, pageable)));
    }
}
