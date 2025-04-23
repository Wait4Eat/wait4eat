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
    public List<OutboxMessage> createNotificationOutboxes(List<NotificationPayload> notificationPayloads) {
        List<OutboxMessage> messages = new ArrayList<>();

        for (NotificationPayload notificationPayload : notificationPayloads) {
            try {
                String payload = objectMapper.writeValueAsString(notificationPayload);
                messages.add(
                        OutboxMessage.builder()
                                .id(notificationPayload.getMessageKey())
                                .aggregateId(notificationPayload.getTargetUserId())
                                .aggregateType("NOTIFICATION")
                                .payload(payload)
                                .build()
                );
            } catch (JsonProcessingException e) {
                log.warn("[OUTBOX 직렬화 실패] notificationId={}, reason={}", notificationPayload.getTargetUserId(), e.getMessage());
            }
        }

        outboxJdbcRepository.saveAll(messages);
        return messages;
    }

    @Transactional
    public List<OutboxMessage> createEventOutboxes(List<MessagePayload> messagePayloads) {
        List<OutboxMessage> messages = new ArrayList<>();

        for (MessagePayload messagePayload : messagePayloads) {
            try {
                String payload = objectMapper.writeValueAsString(messagePayload);
                messages.add(
                        OutboxMessage.builder()
                                .id(messagePayload.getMessageKey())
                                .aggregateId(null)
                                .aggregateType("EVENT")
                                .payload(payload)
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
