package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.batch.processor.DashboardProcessorConfig;
import com.example.wait4eat.domain.dashboard.batch.reader.DashboardReaderConfig;
import com.example.wait4eat.domain.dashboard.batch.writer.*;
import com.example.wait4eat.domain.dashboard.repository.DashboardRepository;
import com.example.wait4eat.domain.dashboard.repository.PopularStoreRepository;
import com.example.wait4eat.domain.dashboard.repository.StoreSalesRankRepository;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Getter
@RequiredArgsConstructor
public class DashboardBatchSupport {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final PaymentRepository paymentRepository;
    private final WaitingRepository waitingRepository;
    private final DashboardRepository dashboardRepository;
    private final PopularStoreRepository popularStoreRepository;
    private final StoreSalesRankRepository storeSalesRankRepository;

    public LocalDate getYesterday() {
        return LocalDate.now().minusDays(1);
    }

    public LocalDateTime getStartDate() {
        return getYesterday().atStartOfDay();
    }

    public LocalDateTime getEndDate() {
        return getYesterday().atTime(LocalTime.MAX);
    }

    @Bean
    public ExecutionContextPromotionListener executionContextPromotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"totalUserCount", "dailyUserCount", "totalStoreCount", "dailyNewStoreCount", "dailyTotalSales"});
        return listener;
    }
}
