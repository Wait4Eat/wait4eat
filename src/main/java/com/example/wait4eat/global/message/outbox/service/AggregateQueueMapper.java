package com.example.wait4eat.global.message.outbox.service;

import com.example.wait4eat.global.message.outbox.enums.AggregateType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AggregateQueueMapper {

    private final Map<String, String> queueMap;

    @Autowired
    public AggregateQueueMapper(
            @Value("${queue.notification.general}") String notificationQueueName,
            @Value("${queue.payment}") String paymentQueueName
    ) {
        this.queueMap = Map.of(
                AggregateType.NOTIFICATION.name(), notificationQueueName,
                AggregateType.PAYMENT_REFUND.name(), paymentQueueName
        );
    }

    public String getQueueName(String aggregateType) {
        String queueName = queueMap.get(aggregateType);
        if (queueName == null) {
            throw new IllegalArgumentException("지원하지 않는 aggregateType입니다: " + aggregateType);
        }
        return queueName;
    }
}
