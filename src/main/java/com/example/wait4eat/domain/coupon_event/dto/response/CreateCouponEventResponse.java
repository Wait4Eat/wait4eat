package com.example.wait4eat.domain.coupon_event.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateCouponEventResponse {

    private final Long id;
    private final Long storeId;
    private final String name;
    private final BigDecimal discountAmount;
    private final Integer totalQuantity;
    private final Integer issuedQuantity;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime expiresAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime createdAt;

    @Builder
    public CreateCouponEventResponse(
            Long id,
            Long storeId,
            String name,
            BigDecimal discountAmount,
            Integer totalQuantity,
            Integer issuedQuantity,
            LocalDateTime expiresAt,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.storeId = storeId;
        this.name = name;
        this.discountAmount = discountAmount;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = issuedQuantity;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

}
