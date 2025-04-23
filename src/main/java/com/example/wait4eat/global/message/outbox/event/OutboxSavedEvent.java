package com.example.wait4eat.global.message.outbox.event;

import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
public class OutboxSavedEvent implements Serializable {

    private final List<OutboxMessage> messages;

    public OutboxSavedEvent(List<OutboxMessage> messages) {
        this.messages = messages;
    }
}
