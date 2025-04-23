package com.example.wait4eat.global.config;

import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.dashboard.repository.DashboardRepository;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.repository.PopularStoreRepository;
import com.example.wait4eat.domain.dashboard.repository.StoreSalesRankRepository;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;

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
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final PaymentRepository paymentRepository;
    private final WaitingRepository waitingRepository;
    private final DashboardRepository dashboardRepository;
    private final PopularStoreRepository popularStoreRepository;
    private final StoreSalesRankRepository storeSalesRankRepository;

    @Bean
    public Job dailyStatisticsJob() {
        return new JobBuilder("dailyStatisticsJob", jobRepository)
                .start(updateDashboardStep())
                .next(updatePopularStoreStep())
                .next(updateStoreSalesRankStep())
                .build();
    }

    @Bean
    public Step updateDashboardStep() {
        return new StepBuilder("updateDashboardStep", jobRepository)
                .tasklet(dashboardUpdateTasklet(), transactionManager)
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
    public Tasklet dashboardUpdateTasklet() {
        return (contribution, chunkContext) -> {
            Long totalUserCount = userRepository.countByRole(UserRole.ROLE_USER);
            Long dailyUserCount = userRepository.countByLoginDateAndRole(getYesterday(), UserRole.ROLE_USER);
            Long totalStoreCount = storeRepository.count();
            Long dailyNewStoreCount = storeRepository.countByCreatedAtBetween(getStartDate(), getEndDate());
            Long dailyTotalSales = paymentRepository.sumSalesByDate(getStartDate(), getEndDate(), PaymentStatus.PAID);

            Dashboard dashboard = Dashboard.builder()
                    .totalUserCount(totalUserCount)
                    .dailyUserCount(dailyUserCount)
                    .totalStoreCount(totalStoreCount)
                    .dailyNewStoreCount(dailyNewStoreCount)
                    .dailyTotalSales(dailyTotalSales)
                    .statisticsDate(LocalDate.now())
                    .build();

            dashboardRepository.save(dashboard);
            return RepeatStatus.FINISHED;
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
            private final LocalDateTime startDate = getStartDate();
            private final LocalDateTime endDate = getEndDate();

            @Override
            public StoreSalesRank process(@NonNull Store store) {
                Long totalSales = paymentRepository.sumSalesByStoreAndCreatedAtBetween(store, getStartDate(), getEndDate());
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
}
