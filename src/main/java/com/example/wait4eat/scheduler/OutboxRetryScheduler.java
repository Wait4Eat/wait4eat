package com.example.wait4eat.scheduler;

import com.example.wait4eat.global.message.publisher.MessagePublisher;
import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import com.example.wait4eat.global.message.outbox.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRetryScheduler { // TODO : 여러 인스턴스에서 동시에 수행되지 않도록 Shed Lock 적용 필요

    private final OutboxMessageRepository outboxMessageRepository;
    private final MessagePublisher messagePublisher;

    private static final int MAX_RETRY_COUNT = 3;
    private static final int BATCH_SIZE = 100;

    @Scheduled(fixedRate = 10000)
    public void retry() {
        List<OutboxMessage> messages = outboxMessageRepository
                .findFailedOutboxByRetryCountLessThanOrderByCreatedAtDesc(
                        MAX_RETRY_COUNT,
                        BATCH_SIZE
                );

        if(messages.isEmpty()) return;

        log.info("[Outbox 재시도 시작] count={}", messages.size());

        for (OutboxMessage message : messages) {
            try {
                messagePublisher.publish(message.getPayload());
                log.info("재발송 성공: id={}", message.getId());
                message.markAsSent();
            } catch (Exception e) {
                message.markAsFailed();
                message.incrementRetryCount();
                log.warn("재발송 실패: type={}, aggregateId={}, reason={}",
                        message.getAggregateType(), message.getAggregateId(), e.getMessage());
            }
        }

        outboxMessageRepository.saveAll(messages);
    }
}
