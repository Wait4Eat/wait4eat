package com.example.wait4eat.global.message.dto;

import com.example.wait4eat.global.message.enums.MessageType;
import com.example.wait4eat.global.message.outbox.enums.AggregateType;
import lombok.Getter;

@Getter
public class EventMessagePublishRequest extends MessagePublishRequest {

    private AggregateType aggregateType;
    private Long targetId;

    public EventMessagePublishRequest(AggregateType aggregateType, MessageType type, Long targetId, String message) {
        super(type, message);
        this.aggregateType = aggregateType;
        this.targetId = targetId;
    }
}
