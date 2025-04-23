package com.example.wait4eat.infra.sqs.publisher;

import com.example.wait4eat.global.message.publisher.MessagePublisher;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SqsMessagePublisher implements MessagePublisher {

    @Value("${spring.cloud.aws.sqs.endpoint}")
    private String endpoint;
    private final SqsTemplate sqsTemplate;

    @Override
    public void publish(String payload) {
        sqsTemplate.send(endpoint, payload);
    }
}