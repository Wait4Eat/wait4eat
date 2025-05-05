package com.example.wait4eat.domain.dashboard.repository;

import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DashboardRepository extends JpaRepository<Dashboard, Long> {
    Optional<Dashboard> findByStatisticsDate(LocalDate targetDate);
    default Dashboard findByStatisticsDateOrElseThrow(LocalDate targetDate) {
        return findByStatisticsDate(targetDate).orElseThrow(
                () -> new CustomException(ExceptionType.DASH_BOARD_NOT_FOUND)
        );
    }

    Page<Dashboard> findByStatisticsDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
