package com.example.wait4eat.domain.waiting.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class WaitingRedisService {

    private final RedisTemplate<String, Long> redisTemplate;
    private static final String WAITING_STORE_KEY_PREFIX = "waiting:store:";
    private static final String WAITING_DATE_FORMAT = "yyyyMMdd";
    private static final LocalTime EXPIRATION_TIME = LocalTime.of(3, 0, 0);

    // ZSet Key 생성 "waiting:store:{storeId}:{yyyyMMdd}"
    public String generateKey(Long storeId) {
        LocalDate today = LocalDate.now();
        return WAITING_STORE_KEY_PREFIX + storeId + ":" + today.format(DateTimeFormatter.ofPattern(WAITING_DATE_FORMAT));
    }

    // 새로운 웨이팅 ID를 추가
    public void addToWaitingZSet(String key, Long waitingId, LocalDateTime activatedAt) {
        // 키가 존재하는지 확인
        Boolean exists = redisTemplate.hasKey(key);
        // activatedAt을 밀리초로 변환한 값을 score로 저장
        redisTemplate.opsForZSet().add(key, waitingId, activatedAt.toInstant(ZoneOffset.UTC).toEpochMilli());
        // 키가 존재하지 않으면 만료 시간 설정
        if (Boolean.FALSE.equals(exists)) {
            setExpiration(key);
        }
    }

    // 특정 웨이팅 ID 제거
    public void removeFromWaitingZSet(String key, Long waitingId) {
        redisTemplate.opsForZSet().remove(key, waitingId);
    }

    // 웨이팅 목록의 첫 번째 순서인지 확인
    public boolean isFirstWaiting(String key, Long waitingId) {
        Set<Long> first = redisTemplate.opsForZSet().range(key, 0, 0);
        return first != null && !first.isEmpty() && first.iterator().next().equals(waitingId);
    }

    // 특정 가게의 웨이팅 팀 수 조회
    public int getWaitingCount(String key) {
        Long count = redisTemplate.opsForZSet().zCard(key);
        return count != null ? count.intValue() : 0;
    }

    // Redis Key 만료 시간 설정
    private void setExpiration(String key) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.toLocalDate().atTime(EXPIRATION_TIME);
        // 현재 시간이 만료 시간 (새벽 3시) 이후라면 다음 날로 설정
        if (now.isAfter(expiration)) {
            expiration = expiration.plusDays(1);
        }
        // 현재 시간과 만료 시간의 차이를 초 단위로 계산하여 만료 시간 설정
        redisTemplate.expire(key, Duration.between(now, expiration).getSeconds(), TimeUnit.SECONDS);
    }
}

