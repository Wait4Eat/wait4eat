package com.example.wait4eat.domain.payment.dto;

import com.example.wait4eat.domain.payment.enums.WebhookPaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class TossWebhookPayload {

    private String eventType;
    private PaymentData data;

    @Getter
    @NoArgsConstructor
    public static class PaymentData {
        private String paymentKey;
        private String orderId;
        private BigDecimal totalAmount;
        private String status;
        private List<CancelInfo> cancels;

        public WebhookPaymentStatus getPaymentStatus() {
            return WebhookPaymentStatus.from(status);
        }
    }

    @Getter
    public static class CancelInfo {
        private BigDecimal cancelAmount;
        private OffsetDateTime canceledAt;
    }
}
