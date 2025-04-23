package com.example.wait4eat.infra.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterManager {

    private final static Long SSE_TIMEOUT_MS = 60 * 1000L * 5; // 5분

    private final Map<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();


    public SseEmitter connect(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitterMap.put(userId, emitter);

        emitter.onCompletion(() -> emitterMap.remove(userId));
        emitter.onTimeout(() -> emitterMap.remove(userId));
        emitter.onError(e -> emitterMap.remove(userId));

        return emitter;
    }

    public void send(Long userId, String message) {
        SseEmitter emitter = emitterMap.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(message));
            } catch (IOException e) {
                log.error("SSE 전송 실패: userId={}", userId, e);
                emitterMap.remove(userId);
            }
        }
    }
}
