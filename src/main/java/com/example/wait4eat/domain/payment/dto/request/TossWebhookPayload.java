package com.example.wait4eat.domain.payment.dto.request;

import com.example.wait4eat.domain.payment.client.dto.TossPaymentData;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossWebhookPayload {
    private String eventType;
    private TossPaymentData data;
}
