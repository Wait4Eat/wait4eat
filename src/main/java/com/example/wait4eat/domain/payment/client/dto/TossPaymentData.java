package com.example.wait4eat.domain.payment.client.dto;

import com.example.wait4eat.domain.payment.enums.TossPaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class TossPaymentData {
    private String paymentKey;
    private String orderId;
    private String status;
    private BigDecimal totalAmount;
    private String method;
    private OffsetDateTime approvedAt;
    private List<CancelInfo> cancels;

    @Getter
    public static class CancelInfo {
        private BigDecimal cancelAmount;
        private OffsetDateTime canceledAt;
    }

    public TossPaymentStatus getPaymentStatus() {
        return TossPaymentStatus.from(status);
    }
}
