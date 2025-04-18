package com.example.wait4eat.global.config;

import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import com.example.wait4eat.domain.dashboard.entity.StoreSalesRank;
import com.example.wait4eat.domain.dashboard.repository.DashboardRepository;
import com.example.wait4eat.domain.dashboard.entity.Dashboard;
import com.example.wait4eat.domain.dashboard.repository.PopularStoreRepository;
import com.example.wait4eat.domain.dashboard.repository.StoreSalesRankRepository;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableBatchProcessing
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
                .tasklet(popularStoreUpdateTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step updateStoreSalesRankStep() {
        return new StepBuilder("updateStoreSalesRankStep", jobRepository)
                .tasklet(storeSalesRankUpdateTasklet(), transactionManager)
                .build();
    }



    @Bean
    public Tasklet dashboardUpdateTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            Long totalUserCount = userRepository.count();
            Long dailyUserCount = userRepository.countByLoginDate(yesterday);
            Long totalStoreCount = storeRepository.count();
            Long dailyNewStoreCount = storeRepository.countByCreatedAt(yesterday);
            Long dailyTotalSales = paymentRepository.sumSalesByDate(yesterday);

            Dashboard dashboard = Dashboard.builder()
                    .totalUserCount(totalUserCount)
                    .dailyUserCount(dailyUserCount)
                    .totalStoreCount(totalStoreCount)
                    .dailyNewStoreCount(dailyNewStoreCount)
                    .dailyTotalSales(dailyTotalSales)
                    .statisticsDate(yesterday)
                    .build();

            dashboardRepository.save(dashboard);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet popularStoreUpdateTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startOfDay = yesterday.atStartOfDay();
            LocalDateTime endOfDay = yesterday.atTime(LocalTime.MAX);

            List<Store> storeList = storeRepository.findTop10StoresByWaitingCount(startOfDay, endOfDay);
            Dashboard findDashboard = dashboardRepository.findByStatisticsDateOrElseThrow(yesterday);
            List<PopularStore> popularStores = new ArrayList<>();

            for (int i = 0; i < storeList.size(); i++) {
                Store store = storeList.get(i);
                int waitingCount = waitingRepository.countByStoreAndCreatedAtBetween(store, startOfDay, endOfDay);

                PopularStore popularStore = PopularStore.builder()
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .waitingCount(waitingCount)
                        .ranking(i+1)
                        .dashboard(findDashboard)
                        .build();

                popularStores.add(popularStore);
            }

            popularStoreRepository.saveAll(popularStores);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet storeSalesRankUpdateTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            List<Store> stores = storeRepository.findAll();
            Dashboard findDashboard = dashboardRepository.findByStatisticsDateOrElseThrow(yesterday);

            List<StoreSalesRank> storeSalesRanks = new ArrayList<>();
            for (int i = 0; i <stores.size(); i++) {
                Store store = stores.get(i);
                Long totalSales = paymentRepository.sumSalesByStoreAndDate(store, yesterday);

                StoreSalesRank storeSalesRank = StoreSalesRank.builder()
                        .storeId(store.getId())
                        .storeName(store.getName())
                        .totalSales(totalSales)
                        .ranking(i+1)
                        .dashboard(findDashboard)
                        .build();

                storeSalesRanks.add(storeSalesRank);
            }

            storeSalesRankRepository.saveAll(storeSalesRanks);
            return RepeatStatus.FINISHED;
        };
    }
}
