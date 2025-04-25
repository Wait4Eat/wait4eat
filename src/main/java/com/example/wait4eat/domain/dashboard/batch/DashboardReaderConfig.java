package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.dto.DashboardStatsAccumulator;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DashboardReaderConfig {
    private final DashboardBatchSupport batchSupport;

    @Bean
    public JpaPagingItemReader<User> userStatsReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<User>()
                .name("userStatsReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT u FROM User u WHERE u.role = :role")
                .parameterValues(Map.of("role", UserRole.ROLE_USER))
                .pageSize(1000)
                .build();
    }

    @Bean
    public ItemReader<Store> storeStatsReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Store>()
                .name("storeStatsReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Store s")
                .pageSize(1000)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Payment> paymentStatsReader() {
        return new JpaPagingItemReaderBuilder<Payment>()
                .name("paymentStatsReader")
                .entityManagerFactory(batchSupport.entityManagerFactory)
                .queryString("SELECT p FROM Payment p WHERE p.status = :status AND p.paidAt BETWEEN :startDate AND :endDate")
                .parameterValues(Map.of(
                        "status", PaymentStatus.PAID,
                        "startDate", batchSupport.getStartDate(),
                        "endDate", batchSupport.getEndDate()
                ))
                .pageSize(1000)
                .build();
    }

    @Bean
    public ItemReader<DashboardStatsAccumulator> accumulatorReader(DashboardStatsAccumulator accumulator) {
        return new ItemReader<>() {
            private boolean read = false;

            @Override
            public DashboardStatsAccumulator read() {
                if (!read) {
                    read = true;
                    return accumulator;
                }
                return null; // 종료 시그널
            }
        };
    }

    @Bean
    @StepScope
    public ListItemReader<Store> popularStoreReader() {
        LocalDateTime startDate = batchSupport.getStartDate();
        LocalDateTime endDate = batchSupport.getEndDate();
        List<Store> popularStores = batchSupport.storeRepository
                .findTop10StoresByWaitingCount(startDate, endDate, PageRequest.of(0, 10));
        return new ListItemReader<>(popularStores);
    }

    @Bean
    @StepScope
    public ListItemReader<Store> storeSalesRankReader() {
        List<Store> stores = batchSupport.storeRepository.findAll();
        return new ListItemReader<>(stores);
    }
}
