package com.example.wait4eat.domain.payment.controller;

import com.example.wait4eat.domain.payment.dto.TossWebhookPayload;
import com.example.wait4eat.domain.payment.service.PaymentVerificationService;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentWebhookController {

    @Value("${webhook.toss.endpoint}")
    private String tossWebhookEndpoint;

    private final PaymentVerificationService paymentVerificationService;

    @PostMapping("${webhook.toss.endpoint}")
    public SuccessResponse tossWebhook(@RequestBody TossWebhookPayload payload) {
        paymentVerificationService.verifyWebhook(payload);
        return SuccessResponse.from(true);
    }
}
