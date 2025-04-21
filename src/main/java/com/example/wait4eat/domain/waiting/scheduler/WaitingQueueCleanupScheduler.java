package com.example.wait4eat.domain.waiting.scheduler;

import com.example.wait4eat.domain.waiting.service.WaitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitingQueueCleanupScheduler {

    private final WaitingService waitingService;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 실행
    public void runCleanupWaitingQueuesJob() {
        log.info("웨이팅 큐 정리 스케줄러 실행: {}", LocalDateTime.now());
        waitingService.cleanupWaitingQueues();
        log.info("웨이팅 큐 정리 스케줄러 종료: {}", LocalDateTime.now());
    }
}
