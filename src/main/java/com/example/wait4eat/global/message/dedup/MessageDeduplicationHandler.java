package com.example.wait4eat.global.message.dedup;

public interface MessageDeduplicationHandler {
    boolean isDuplicated(String messageKey);
    void markAsProcessed(String messageKey);
}
