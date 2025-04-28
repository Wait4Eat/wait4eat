package com.example.wait4eat.domain.payment.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossErrorResponse {
    private String code;
    private String message;
}
