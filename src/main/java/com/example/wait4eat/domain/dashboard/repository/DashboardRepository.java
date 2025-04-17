package com.example.wait4eat.domain.dashboard.repository;

import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
}
