package com.example.wait4eat.global.message.dedup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Primary
@Component
@RequiredArgsConstructor
public class CompositeMessageDeduplicationHandler implements MessageDeduplicationHandler {

    private final RedisMessageDeduplicationHandler redisHandler;
    private final DbMessageDeduplicationHandler dbHandler;

    @Transactional(readOnly = true)
    public boolean isDuplicated(String messageKey) {
        if (redisHandler.isDuplicated(messageKey)) {
            return true;
        }
        return dbHandler.isDuplicated(messageKey);
    }

    @Transactional
    public void markAsProcessed(String messageKey) {
        try {
            dbHandler.markAsProcessed(messageKey);
            redisHandler.markAsProcessed(messageKey);
        } catch (Exception e) {
            log.error("Inbox 마킹 실패: {}", messageKey, e);
            throw new RuntimeException(e); // 재시도 유도
        }

        // log.info("messageKey 처리 마킹 완료: {}", messageKey);
    }
}