package com.example.wait4eat.domain.dashboard.repository;

import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopularStoreRepository extends JpaRepository<PopularStore, Long> {
    List<PopularStore> findByDashboardIdOrderByRanking(Long dashboardId);
}
