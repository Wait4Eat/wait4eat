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
    private String openAiApiKey;
    private final WebClient.Builder webClientBuilder;

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @SuppressWarnings("unchecked")
    public boolean isNegativeContent(String content) {
        WebClient webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "다음 메시지에 욕설, 부적절하거나 폭력적인 표현이 포함되어 있는지 판단해서 true 또는 false로만 대답해줘."),
                        Map.of("role", "user", "content", content)
                ),
                "max_tokens", 10,
                "temperature", 0.0
        );

        Mono<Boolean> response = webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(responseBody -> {
                    try {
                        List<Map<String, Object>> choices =(List<Map<String, Object>>) responseBody.get("choices");
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        String reply = ((String) message.get("content")).trim().toLowerCase();
                        return reply.contains("true");
                    } catch (Exception e) {
                        return false;
                    }
                });

        return Boolean.TRUE.equals(response.block());
    }
}
