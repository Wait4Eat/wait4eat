package com.example.wait4eat.global.message.outbox.service;

import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxPublisher outboxPublisher;
    static final int BATCH_SIZE = 100;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(List<OutboxMessage> messages) {
        List<List<OutboxMessage>> batches = partition(messages);

        for (List<OutboxMessage> batch : batches) {
            outboxPublisher.publishBatch(batch);
        }
    }

    private List<List<OutboxMessage>> partition(List<OutboxMessage> messages) {
        List<List<OutboxMessage>> result = new ArrayList<>();
        for (int i = 0; i < messages.size(); i += BATCH_SIZE) {
            result.add(messages.subList(i, Math.min(i + BATCH_SIZE, messages.size())));
        }
        return result;
    }
}
