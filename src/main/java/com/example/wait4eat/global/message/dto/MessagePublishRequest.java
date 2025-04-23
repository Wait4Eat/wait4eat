package com.example.wait4eat.global.message.dto;

import com.example.wait4eat.global.message.enums.MessageType;
import com.example.wait4eat.global.message.payload.MessagePayload;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class MessagePublishRequest {

    private final MessageType type;
    private final String message;

    public MessagePublishRequest(MessageType type, String message) {
        this.type = type;
        this.message = message;
    }
}
