package com.example.wait4eat.client;

import com.example.wait4eat.domain.payment.entity.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    @Value("${slack.web-hook-url}")
    private String slackWebhookUrl;
    private RestTemplate restTemplate = new RestTemplate();

    // TODO : 조금 더 구조화 필요
    public void sendNotificationToSlack(String message) {
        String payload = "{\"text\" : \"" + message + "\"}";
        restTemplate.postForObject(slackWebhookUrl, payload, String.class);
        log.info("Slack notification sent to slack webhook: {}", payload);
    }

    public void sendRefundFailedNotification(Payment payment, String reason) {
        String message = String.format(
                "[Failed to Refund] paymentId=%d, paymentKey=%s, reason=%s",
                payment.getId(), payment.getPaymentKey(), reason
        );
        sendNotificationToSlack(message);
    }
}
