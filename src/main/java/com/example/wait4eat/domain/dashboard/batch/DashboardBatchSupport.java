package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.dto.DashboardStatsAccumulator;
import com.example.wait4eat.domain.dashboard.repository.DashboardRepository;
import com.example.wait4eat.domain.dashboard.repository.PopularStoreRepository;
import com.example.wait4eat.domain.dashboard.repository.StoreSalesRankRepository;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DashboardBatchSupport {
    protected final JobRepository jobRepository;
    protected final PlatformTransactionManager transactionManager;
    protected final EntityManagerFactory entityManagerFactory;
    protected final StoreRepository storeRepository;
    protected final PaymentRepository paymentRepository;
    protected final WaitingRepository waitingRepository;
    protected final DashboardRepository dashboardRepository;
    protected final PopularStoreRepository popularStoreRepository;
    protected final StoreSalesRankRepository storeSalesRankRepository;

    protected LocalDate getYesterday() {
        return LocalDate.now().minusDays(1);
    }

    protected LocalDateTime getStartDate() {
        return getYesterday().atStartOfDay();
    }

    protected LocalDateTime getEndDate() {
        return getYesterday().atTime(LocalTime.MAX);
    }

    @Bean
    @JobScope
    protected DashboardStatsAccumulator dashboardStatsAccumulator() {
        return new DashboardStatsAccumulator();
    }
}
