package com.example.wait4eat.global.message.dedup;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisMessageDeduplicationHandler implements MessageDeduplicationHandler {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofHours(1);
    private static final String DEDUP_MESSAGE_PREFIX = "dedup:message:";

    @Override
    public boolean isDuplicated(String messageKey) {
        String key = getKey(messageKey);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void markAsProcessed(String messageKey) {
        String key = getKey(messageKey);
        redisTemplate.opsForValue().set(key, "1", TTL);
    }

    private String getKey(String messageKey) {
        return DEDUP_MESSAGE_PREFIX + messageKey;
    }
}
