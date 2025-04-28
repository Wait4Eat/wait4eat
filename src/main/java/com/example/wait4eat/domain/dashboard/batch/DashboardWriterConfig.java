package com.example.wait4eat.domain.dashboard.batch;

import com.example.wait4eat.domain.dashboard.dto.DashboardStatsAccumulator;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
@RequiredArgsConstructor
public class DashboardWriterConfig {
    private final DashboardBatchSupport batchSupport;

    @Bean
    public ItemWriter<User> userStatsWriter(DashboardStatsAccumulator accumulator) {
        return users -> {
            for (User user : users) {
                boolean isLoginYesterday = user.getLastLoginDate() != null && user.getLastLoginDate().equals(batchSupport.getYesterday());

                accumulator.addUser(isLoginYesterday);
            }
        };
    }

    @Bean
    public ItemWriter<Store> storeStatsWriter(DashboardStatsAccumulator accumulator) {
        return stores -> {
            for (Store store : stores) {
                boolean isCreatedYesterday = store.getCreatedAt() != null &&
                        store.getCreatedAt().toLocalDate().equals(batchSupport.getYesterday());

                accumulator.addStore(isCreatedYesterday);
            }
        };
    }

    @Bean
    public ItemWriter<Payment> paymentStatsWriter(DashboardStatsAccumulator accumulator) {
        return payments -> {
            BigDecimal totalSales = payments.getItems().stream()
                    .map(Payment::getAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            accumulator.addPayment(totalSales);
        };
    }

    @Bean
    @StepScope
    public ItemWriter<StoreSalesRank> storeSalesRankWriter() {
        return items -> {
            List<StoreSalesRank> itemList = StreamSupport.stream(items.spliterator(), false)
                    .sorted(Comparator.comparing(StoreSalesRank::getTotalSales).reversed())
                    .collect(Collectors.toList());

            for (int i = 0; i < itemList.size(); i++) {
                itemList.get(i).setRanking(i+1);
            }

            batchSupport.storeSalesRankRepository.saveAll(itemList);
        };
    }
}
