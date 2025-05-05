package com.example.wait4eat.domain.dashboard.repository;

import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreSalesRankRepository extends JpaRepository<StoreSalesRank, Long> {
    Page<StoreSalesRank> findByDashboardOrderByRanking(Dashboard dashboard, Pageable pageable);
}
