package com.example.wait4eat.global.message.outbox.service;

import com.example.wait4eat.global.message.payload.MessagePayload;
import com.example.wait4eat.global.message.payload.NotificationPayload;
import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import com.example.wait4eat.global.message.outbox.repository.OutboxJdbcRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final ObjectMapper objectMapper;
    private final OutboxJdbcRepository outboxJdbcRepository;

    @Transactional
    public List<OutboxMessage> createOutboxes(List<? extends MessagePayload> payloads) {
        List<OutboxMessage> messages = new ArrayList<>();

        for (MessagePayload payload : payloads) {
            try {
                String stringPayload = objectMapper.writeValueAsString(payload);
                messages.add(
                        OutboxMessage.builder()
                                .id(payload.getMessageKey())
                                .aggregateId(payload.getAggregateId())
                                .aggregateType(payload.getAggregateType())
                                .payload(stringPayload)
                                .build()
                );
            } catch (JsonProcessingException e) {
                log.warn("[OUTBOX 직렬화 실패] reason={}", e.getMessage());
            }
        }

        outboxJdbcRepository.saveAll(messages);
        return messages;
    }
}
