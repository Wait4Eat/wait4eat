package com.example.wait4eat.domain.coupon_event.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateCouponEventRequest {

    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    private String name;

    @NotNull(message = "할인 금액은 필수입니다.")
    @Positive(message = "할인 금액은 0보다 커야 합니다.")
    private BigDecimal discountAmount;

    @NotNull(message = "총 수량은 필수입니다.")
    @Positive(message = "총 수량은 0보다 커야 합니다.")
    private Integer totalQuantity;

    @NotNull(message = "쿠폰 만료일은 필수입니다.")
    @Future(message = "만료일은 현재 시간 이후여야 합니다.")
    private LocalDateTime expiresAt;

}
