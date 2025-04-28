package com.example.wait4eat.domain.payment.client.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class TossCancelPaymentResponse {

    private List<CancelInfo> cancels;

    @Getter
    public static class CancelInfo {
        private BigDecimal cancelAmount;
        private OffsetDateTime canceledAt;
    }
}
