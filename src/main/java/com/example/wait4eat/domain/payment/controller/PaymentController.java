package com.example.wait4eat.domain.payment.controller;

import com.example.wait4eat.domain.payment.dto.request.ConfirmPaymentRequest;
import com.example.wait4eat.domain.payment.dto.request.PreparePaymentRequest;
import com.example.wait4eat.domain.payment.dto.request.RefundPaymentRequest;
import com.example.wait4eat.domain.payment.dto.response.PreparePaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.RefundPaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.SuccessPaymentResponse;
import com.example.wait4eat.domain.payment.service.PaymentService;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import com.example.wait4eat.global.dto.response.ErrorResponse;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/v1/payments/prepare")
    public ResponseEntity<SuccessResponse<PreparePaymentResponse>> createPaymentPrepare(
            @RequestBody PreparePaymentRequest request
    ) {
        PreparePaymentResponse response = paymentService.preparePayment(request);
        return ResponseEntity.ok(SuccessResponse.from(response));
    }

    @PostMapping("/api/v1/payments/confirm")
    public ResponseEntity<SuccessResponse<SuccessPaymentResponse>> handlePaymentSuccess(
            @RequestBody ConfirmPaymentRequest request
    ) {
        SuccessPaymentResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(SuccessResponse.of(response, "웨이팅이 확정되었습니다."));
    }
}