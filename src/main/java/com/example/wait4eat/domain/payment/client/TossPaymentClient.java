package com.example.wait4eat.domain.payment.client;

import com.example.wait4eat.domain.payment.client.dto.TossCancelPaymentResponse;
import com.example.wait4eat.domain.payment.client.dto.TossConfirmPaymentResponse;
import com.example.wait4eat.domain.payment.client.dto.TossErrorResponse;
import com.example.wait4eat.domain.payment.client.exception.TossPaymentErrorException;
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

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MILLIS = 100;

    // TODO : 추후 WebClient로 고도화
    public TossConfirmPaymentResponse confirmPayment(String paymentKey, String orderId, BigDecimal amount) {
        String url = "https://api.tosspayments.com/v1/payments/confirm";

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        return sendPostRequest(url, body, TossConfirmPaymentResponse.class, false);
    }

    public TossCancelPaymentResponse cancelPayment(String paymentKey, String cancelReason) {
        String url = "https://api.tosspayments.com/v1/payments/"+paymentKey+"/cancel";

        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", cancelReason);

        return sendPostRequest(url, body, TossCancelPaymentResponse.class, true);
    }

    private <T> T sendPostRequest(String url, Map<String, Object> body, Class<T> responseType, boolean retry) {
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        int attempts = 0;
        while (true) {
            attempts++;
            try {
                ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, request, responseType);
                return response.getBody();
            } catch (HttpClientErrorException e) {
                log.error("[Toss API 오류] {}회차, 상태코드={}, URL={}, 요청Body={}",
                        attempts, e.getStatusCode(), url, body);

                if (e.getStatusCode().is4xxClientError() || !retry || attempts >= MAX_RETRIES) {
                    throw parseTossPaymentError(e);
                }

                sleep(RETRY_DELAY_MILLIS);
            } catch (Exception e) {
                log.error("[Toss API 통신 실패] {}회차: {}", attempts, e.getMessage());
                if (!retry || attempts >= MAX_RETRIES) {
                    throw new RuntimeException("Toss API 통신 실패", e);
                }
                sleep(RETRY_DELAY_MILLIS);
            }
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String encodedKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedKey);
        return headers;
    }

    private TossPaymentErrorException parseTossPaymentError(HttpClientErrorException e) {
        try {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody == null || responseBody.isBlank()) {
                throw new RuntimeException("Toss API 응답이 비어 있습니다.");
            }

            TossErrorResponse errorResponse = objectMapper.readValue(responseBody, TossErrorResponse.class);
            return new TossPaymentErrorException(errorResponse.getCode(), errorResponse.getMessage());
        } catch (Exception ex) {
            throw new RuntimeException("Toss API 에러 파싱 실패", ex);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread sleep 중 인터럽트 발생", ie);
        }
    }
}