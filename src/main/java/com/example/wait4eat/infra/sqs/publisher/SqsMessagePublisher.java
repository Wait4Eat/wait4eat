package com.example.wait4eat.infra.sqs.publisher;

import com.example.wait4eat.global.message.publisher.MessagePublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqsMessagePublisher implements MessagePublisher {

    private final SqsTemplate sqsTemplate;

    @Override
    public void publish(String endpoint, String payload) {
        sqsTemplate.send(endpoint, payload);
    }
}