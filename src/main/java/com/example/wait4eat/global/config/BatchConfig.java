package com.example.wait4eat.global.config;

import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.dashboard.repository.DashboardRepository;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.repository.PopularStoreRepository;
import com.example.wait4eat.domain.dashboard.repository.StoreSalesRankRepository;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import com.example.wait4eat.global.dto.DashboardStatsAccumulator;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final StoreRepository storeRepository;
    private final PaymentRepository paymentRepository;
    private final WaitingRepository waitingRepository;
    private final DashboardRepository dashboardRepository;
    private final PopularStoreRepository popularStoreRepository;
    private final StoreSalesRankRepository storeSalesRankRepository;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job dailyStatisticsJob(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .start(userStatsStep(dashboardStatsAccumulator))
                .next(storeStatsStep(dashboardStatsAccumulator))
                .next(paymentStatsStep(dashboardStatsAccumulator))
                .next(saveDashboardStep(dashboardStatsAccumulator))
                .next(updatePopularStoreStep())
                .next(updateStoreSalesRankStep())
                .build();
    }

    @Bean
    public Step userStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("userStatsStep", jobRepository)
                .<User, User>chunk(1000, transactionManager)
                .reader(userStatsReader(entityManagerFactory))
                .writer(userStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean
    public Step storeStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("storeStatsStep", jobRepository)
                .<Store, Store>chunk(1000, transactionManager)
                .reader(storeStatsReader(entityManagerFactory))
                .writer(storeStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean Step paymentStatsStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("paymentStatsStep", jobRepository)
                .<Payment, Payment>chunk(1000, transactionManager)
                .reader(paymentStatsReader())
                .writer(paymentStatsWriter(dashboardStatsAccumulator))
                .build();
    }

    @Bean
    public Step saveDashboardStep(DashboardStatsAccumulator dashboardStatsAccumulator) {
        return new StepBuilder("saveDashboardStep", jobRepository)
                .<DashboardStatsAccumulator, Dashboard>chunk(1, transactionManager)
                .reader(accumulatorReader(dashboardStatsAccumulator))
                .processor(dashboardStatsAccumulatorProcessor())
                .writer(dashboardWriter())
                .build();
    }

    @Bean
    public Step updatePopularStoreStep() {
        return new StepBuilder("updatePopularStoreStep", jobRepository)
                .<Store, PopularStore>chunk(10, transactionManager)
                .reader(popularStoreReader())
                .processor(popularStoreProcessor())
                .writer(popularStoreWriter())
                .build();
    }

    @Bean
    public Step updateStoreSalesRankStep() {
        return new StepBuilder("updateStoreSalesRankStep", jobRepository)
                .<Store, StoreSalesRank>chunk(1000, transactionManager)
                .reader(storeSalesRankReader())
                .processor(storeSalesRankProcessor())
                .writer(storeSalesRankWriter())
                .build();
    }

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
    public ItemWriter<User> userStatsWriter(DashboardStatsAccumulator accumulator) {
        return users -> {
            for (User user : users) {
                boolean isLoginYesterday = user.getLoginDate() != null && user.getLoginDate().equals(getYesterday());

                accumulator.addUser(isLoginYesterday);
            }
        };
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
    public ItemWriter<Store> storeStatsWriter(DashboardStatsAccumulator accumulator) {
        return stores -> {
            for (Store store : stores) {
                boolean isCreatedYesterday = store.getCreatedAt() != null && store.getCreatedAt().toLocalDate().equals(getYesterday());

                accumulator.addStore(isCreatedYesterday);
            }
        };
    }

    @Bean
    public JpaPagingItemReader<Payment> paymentStatsReader() {
        return new JpaPagingItemReaderBuilder<Payment>()
                .name("paymentStatsReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT p FROM Payment p WHERE p.status = :status AND p.paidAt BETWEEN :startDate AND :endDate")
                .parameterValues(Map.of(
                        "status", PaymentStatus.PAID,
                        "startDate", getStartDate(),
                        "endDate", getEndDate()
                ))
                .pageSize(1000)
                .build();
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
    public ItemProcessor<DashboardStatsAccumulator, Dashboard> dashboardStatsAccumulatorProcessor() {
        return new ItemProcessor<DashboardStatsAccumulator, Dashboard>() {
            @Override
            public Dashboard process(@NonNull DashboardStatsAccumulator accumulator) throws Exception {
                return Dashboard.builder()
                        .totalUserCount(accumulator.getTotalUserCount())
                        .dailyUserCount(accumulator.getDailyUserCount())
                        .totalStoreCount(accumulator.getTotalStoreCount())
                        .dailyNewStoreCount(accumulator.getDailyNewStoreCount())
                        .dailyTotalSales(accumulator.getTotalDailySales())
                        .statisticsDate(getYesterday())
                        .build();
            }
        };
    }

    @Bean
    public ItemWriter<Dashboard> dashboardWriter() {
        return new ItemWriter<Dashboard>() {
            @Override
            public void write(@NonNull Chunk<? extends Dashboard> items) throws Exception {
                dashboardRepository.saveAll(items);
            }
        };
    }

    @Bean
    @StepScope
    public ListItemReader<Store> popularStoreReader() {
        LocalDateTime startDate = getStartDate();
        LocalDateTime endDate = getEndDate();
        List<Store> popularStores = storeRepository.findTop10StoresByWaitingCount(startDate, endDate, PageRequest.of(0, 10));
        return new ListItemReader<>(popularStores);
    }

    @Bean
    @StepScope
    public ItemProcessor<Store, PopularStore> popularStoreProcessor() {
        return new ItemProcessor<>() {
            private int rank = 1;

            @Override
            public PopularStore process(@NonNull Store store) {
                int waitingCount = waitingRepository.countByStoreAndCreatedAtBetween(store, getStartDate(), getEndDate());
                Dashboard dashboard = dashboardRepository.findByStatisticsDateOrElseThrow(getYesterday());

                return PopularStore.builder()
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .waitingCount(waitingCount)
                        .ranking(rank++)
                        .dashboard(dashboard)
                        .build();
            }
        };
    }

    @Bean
    @StepScope
    public ItemWriter<PopularStore> popularStoreWriter() {
        return popularStoreRepository::saveAll;
    }

    @Bean
    @StepScope
    public ListItemReader<Store> storeSalesRankReader() {
        List<Store> stores = storeRepository.findAll();
        return new ListItemReader<>(stores);
    }

    @Bean
    @StepScope
    public ItemProcessor<Store, StoreSalesRank> storeSalesRankProcessor() {
        return new ItemProcessor<>() {
            @Override
            public StoreSalesRank process(@NonNull Store store) {
                BigDecimal totalSales = paymentRepository.sumAmountByStoreAndCreatedAtBetween(store, getStartDate(), getEndDate());
                Dashboard findDashboard = dashboardRepository.findByStatisticsDateOrElseThrow(getYesterday());

                return StoreSalesRank.builder()
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .totalSales(totalSales)
                        .dashboard(findDashboard)
                        .ranking(0)
                        .build();
            }
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

            storeSalesRankRepository.saveAll(itemList);
        };
    }

    private LocalDate getYesterday() {
        return LocalDate.now().minusDays(1);
    }

    private LocalDateTime getStartDate() {
        return getYesterday().atStartOfDay();
    }

    private LocalDateTime getEndDate() {
        return getYesterday().atTime(LocalTime.MAX);
    }

    @Bean
    @JobScope
    public DashboardStatsAccumulator dashboardStatsAccumulator() {
        return new DashboardStatsAccumulator();
    }
}
