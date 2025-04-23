package com.example.wait4eat.infra.redis.publisher;

import com.example.wait4eat.global.message.payload.NotificationPayload;
import com.example.wait4eat.domain.notification.publisher.NotificationPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotificationPublisher implements NotificationPublisher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ChannelTopic channelTopic;

    @Override
    public void publish(NotificationPayload payload) {
        try {
            String message = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend(channelTopic.getTopic(), message);
        } catch (JsonProcessingException e) {
            log.error("Redis publish 실패: {}", e.getMessage(), e);
            throw new RuntimeException(e); // 재처리 유도
        }
    }
}
