package com.example.wait4eat.domain.review.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api-key}")
    private String openAiApiKey;  // 실제 API 키로 교체
    private final WebClient.Builder webClientBuilder;

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public boolean isNegativeContent(String content) {
        String apiUrl = "https://api.openai.com/v1/moderations";

        WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();

        Mono<Boolean> response = webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("input", content))
                .retrieve()
                .bodyToMono(Map.class)
                .map(responseBody -> {
                    Object resultsObj = responseBody.get("results");

                    if (resultsObj instanceof List<?> resultsList && !resultsList.isEmpty()) {
                        Object firstResult = resultsList.get(0);
                        if (firstResult instanceof Map<?, ?> resultMap) {
                            Object flagged = resultMap.get("flagged");
                            return flagged instanceof Boolean && (Boolean) flagged;
                        }
                    }
                    return false;
                });

        return Boolean.TRUE.equals(response.block());
    }
}
