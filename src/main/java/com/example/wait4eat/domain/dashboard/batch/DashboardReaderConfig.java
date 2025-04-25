package com.example.wait4eat.domain.dashboard.batch;

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
    public ItemReader<User> userStatsReader(EntityManagerFactory entityManagerFactory) {
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
    public ItemReader<Payment> paymentStatsReader() {
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
    @StepScope
    public JpaPagingItemReader<Store> storeReader() {
        return new JpaPagingItemReaderBuilder<Store>()
                .name("storeSalesRankReader")
                .entityManagerFactory(batchSupport.entityManagerFactory)
                .queryString("SELECT s FROM STORE S")
                .pageSize(1000)
                .build();
    }
}
