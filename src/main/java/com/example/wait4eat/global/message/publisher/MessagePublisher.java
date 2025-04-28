package com.example.wait4eat.global.message.publisher;

public interface MessagePublisher {
    void publish(String endpoint, String message);
}
