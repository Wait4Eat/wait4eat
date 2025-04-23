package com.example.wait4eat.infra.redis.subscriber;

import com.example.wait4eat.global.message.payload.NotificationPayload;
import com.example.wait4eat.infra.sse.SseEmitterManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SseEmitterManager sseEmitterManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody());
            NotificationPayload payload = objectMapper.readValue(json, NotificationPayload.class);
            log.debug("Redis 메시지 수신: userId={}, type={}", payload.getTargetUserId(), payload.getNotificationType());

            sseEmitterManager.send(payload.getTargetUserId(), payload.getMessage());
        } catch (JsonProcessingException e) {
            log.error("Redis 메시지 파싱 실패", e);
        } catch (Exception e) {
            log.error("SSE 알림 전송 실패", e);
        }
    }
}
