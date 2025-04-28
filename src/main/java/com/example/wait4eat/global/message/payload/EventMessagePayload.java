package com.example.wait4eat.global.message.payload;

import com.example.wait4eat.global.message.outbox.enums.AggregateType;
import lombok.Getter;

@Getter
public class EventMessagePayload implements MessagePayload {

    private final String messageKey;
    private final AggregateType aggregateType;
    private final Long targetId;
    private final String message;

    public EventMessagePayload(String messageKey, AggregateType aggregateType, Long paymentId, String message) {
        this.messageKey = messageKey;
        this.aggregateType = aggregateType;
        this.targetId = paymentId;
        this.message = message;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Long getAggregateId() {
        return targetId;
    }

    public String getAggregateType() {
        return this.aggregateType.name();
    }
}
