package com.example.wait4eat.domain.payment.dto.request;

import lombok.*;

@Getter
@NoArgsConstructor
public class PreparePaymentRequest {
    private Long waitingId;
    private Long couponId;
}