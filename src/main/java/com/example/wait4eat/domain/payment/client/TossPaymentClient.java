package com.example.wait4eat.domain.payment.client;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void confirmPayment(String paymentKey, String orderId, BigDecimal amount) {
        String url = "https://api.tosspayments.com/v1/payments/" + paymentKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String encodedKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedKey);
        Map<String, Object> body = new HashMap<>();
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(url, request, String.class);
    }
}

