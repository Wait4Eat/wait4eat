package com.example.wait4eat.scheduler;

import com.example.wait4eat.global.message.outbox.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxExpirationScheduler {

    private final OutboxMessageRepository outboxMessageRepository;

    /**
     * 즉시 발행에 실패한 메시지를 FAILED로 자동 보정
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void markOldPendingMessagesAsFailed() {
        int updatedCount = outboxMessageRepository.markPendingAsFailedBefore(LocalDateTime.now().minusMinutes(1));
        log.info("[OutboxExpirationScheduler] 1분 이상 PENDING 상태인 메시지 {}건을 FAILED로 변경", updatedCount);

    }
}
