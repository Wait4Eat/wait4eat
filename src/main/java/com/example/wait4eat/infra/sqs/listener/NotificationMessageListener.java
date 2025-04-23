package com.example.wait4eat.infra.sqs.listener;

import com.example.wait4eat.global.message.dedup.MessageDeduplicationHandler;
import com.example.wait4eat.global.message.payload.NotificationPayload;
import com.example.wait4eat.global.message.service.NotificationMessageProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageListener {

    @Value("${queue.notification.general}")
    private String queueName;
    private final ObjectMapper objectMapper;
    private final MessageDeduplicationHandler messageDeduplicationHandler;
    private final NotificationMessageProcessor notificationMessageProcessor;

    @SqsListener("${queue.notification.general}")
    public void receive(String rawMessage) {
        try {
            NotificationPayload payload =
                    objectMapper.readValue(rawMessage, NotificationPayload.class);

            if (messageDeduplicationHandler.isDuplicated(payload.getMessageKey())) {
                return; // 중복 메시지 무시
            }

            notificationMessageProcessor.handleNotificationPublish(payload);
        } catch (Exception e) {
            log.error("메시지 처리 실패: {}", rawMessage, e);
            throw new RuntimeException("재시도 유도용 예외", e);
        }
    }
}
