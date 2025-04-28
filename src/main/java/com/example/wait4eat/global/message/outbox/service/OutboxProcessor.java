package com.example.wait4eat.global.message.outbox.service;

import com.example.wait4eat.global.message.publisher.MessagePublisher;
import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import com.example.wait4eat.global.message.outbox.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 직접적으로 message publisher에 요청하는 역할
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final MessagePublisher publisher;
    private final OutboxMessageRepository outboxMessageRepository;
    private final AggregateQueueMapper aggregateQueueMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(List<OutboxMessage> messages) {

        List<String> successIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();

        for (OutboxMessage message : messages) {
            try {
                String payload = message.getPayload();
                String aggregateType = message.getAggregateType();

                String queueName = aggregateQueueMapper.getQueueName(aggregateType);
                publisher.publish(queueName, payload);

                successIds.add(message.getId());
            } catch (Exception e) {
                failedIds.add(message.getId());
                log.warn("즉시 발송 실패: type={}, reason={}",
                        message.getAggregateType(), e.getMessage());
            }
        }

        if (!successIds.isEmpty()) {
            outboxMessageRepository.markAllAsSent(successIds, LocalDateTime.now());
        }
        if (!failedIds.isEmpty()) {
            outboxMessageRepository.markAllAsFailed(failedIds);
        }
    }
}
