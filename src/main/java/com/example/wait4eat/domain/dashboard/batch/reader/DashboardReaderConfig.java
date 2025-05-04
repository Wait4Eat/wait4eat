package com.example.wait4eat.domain.dashboard.batch.reader;

import com.example.wait4eat.domain.dashboard.batch.DashboardBatchSupport;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DashboardReaderConfig {
    private final DashboardBatchSupport batchSupport;

    @Bean
    public ItemReader<User> userStatsReader() {
        return new JpaPagingItemReaderBuilder<User>()
                .name("userStatsReader")
                .entityManagerFactory(batchSupport.getEntityManagerFactory())
                .queryString("SELECT u FROM User u WHERE u.role = :role")
                .parameterValues(Map.of("role", UserRole.ROLE_USER))
                .pageSize(1000)
                .build();
    }

    @Bean
    public ItemReader<Store> storeReader() {
        return new JpaPagingItemReaderBuilder<Store>()
                .name("storeStatsReader")
                .entityManagerFactory(batchSupport.getEntityManagerFactory())
                .queryString("SELECT s FROM Store s")
                .pageSize(1000)
                .build();
    }

    @Bean
    public ItemReader<Payment> paymentStatsReader() {
        return new JpaPagingItemReaderBuilder<Payment>()
                .name("paymentStatsReader")
                .entityManagerFactory(batchSupport.getEntityManagerFactory())
                .queryString("SELECT p FROM Payment p WHERE p.status = :status AND p.paidAt BETWEEN :startDate AND :endDate")
                .parameterValues(Map.of(
                        "status", PaymentStatus.SUCCEEDED,
                        "startDate", batchSupport.getStartDate(),
                        "endDate", batchSupport.getEndDate()
                ))
                .pageSize(1000)
                .build();
    }

    @Bean
    public ItemReader<StoreSalesRank> storeSalesRankReader() {
        return new JpaPagingItemReaderBuilder<StoreSalesRank>()
                .name("storeSalesRankReader")
                .entityManagerFactory(batchSupport.getEntityManagerFactory())
                .queryString("SELECT s FROM StoreSalesRank s WHERE s.dashboard.statisticsDate = :yesterday ORDER BY s.totalSales DESC")
                .parameterValues(Map.of("yesterday", batchSupport.getYesterday()))
                .pageSize(1000)
                .build();
    }
}
