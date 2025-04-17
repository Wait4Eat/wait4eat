package com.example.wait4eat.domain.dashboard.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardScheduler {
    private final JobLauncher jobLauncher;
    private final Job dashboardUpdateJob;

    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 실행
    public void runDailyDashboardJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(dashboardUpdateJob, jobParameters);
            log.info("대시보드 배치 실행 완료");
        } catch (Exception e) {
            log.error("대시보드 배치 실행 실패", e);
        }
    }
}
