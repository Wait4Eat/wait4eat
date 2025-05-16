package com.example.wait4eat.global.message.outbox.service;

import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import com.example.wait4eat.global.message.outbox.repository.OutboxMessageRepository;
import com.example.wait4eat.global.message.publisher.MessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final MessagePublisher publisher;
    private final OutboxMessageRepository outboxMessageRepository;
    private final AggregateQueueMapper aggregateQueueMapper;

    @Async("outboxExecutor")
    @Transactional
    public void publishBatch(List<OutboxMessage> messages) {
        String threadName = Thread.currentThread().getName();
        log.info("[OUTBOX-PUBLISHER] 시작 - thread={}", threadName);

        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();

        for (OutboxMessage message : messages) {
            try {
                String queueName = aggregateQueueMapper.getQueueName(message.getAggregateType());
                publisher.publish(queueName, message.getPayload());
                successIds.add(message.getId());
            } catch (Exception e) {
                log.warn("Failed to publish outbox : id={}, reason={}", message.getId(), e.getMessage());
                failedIds.add(message.getId());
            }
        }

        if (!successIds.isEmpty()) {
            outboxMessageRepository.markAllAsSent(successIds, LocalDateTime.now());
            log.info("Published {} messages successfully on thread {}", successIds.size(), threadName);
        }
        if (!failedIds.isEmpty()) {
            outboxMessageRepository.markAllAsFailed(failedIds);
            log.warn("Failed to publish {} messages on thread {}", failedIds.size(), threadName);
        }
    }
}
