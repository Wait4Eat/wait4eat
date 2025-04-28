package com.example.wait4eat.domain.payment.client;

import com.example.wait4eat.domain.payment.client.dto.TossConfirmPaymentResponse;
import com.example.wait4eat.domain.payment.client.dto.TossErrorResponse;
import com.example.wait4eat.domain.payment.client.exception.TossPaymentConfirmFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    // TODO : 추후 WebClient로 고도화
    public TossConfirmPaymentResponse confirmPayment(String paymentKey, String orderId, BigDecimal amount) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";
        HttpHeaders headers = getHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TossConfirmPaymentResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, TossConfirmPaymentResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("Toss API 에러 응답: {}", e.getResponseBodyAsString());

            try {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody == null || responseBody.isBlank()) {
                    throw new RuntimeException("Toss API 응답이 비어 있습니다.");
                }

                TossErrorResponse errorResponse = objectMapper.readValue(
                        responseBody, TossErrorResponse.class
                );

                throw new TossPaymentConfirmFailedException(
                        errorResponse.getCode(),
                        errorResponse.getMessage()
                );
            } catch (Exception ex) {
                throw new RuntimeException("Toss API 에러 파싱 실패", ex);
            }
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String encodedKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedKey);
        return headers;
    }
}