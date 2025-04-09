package com.example.wait4eat.domain.payment.controller;

import com.example.wait4eat.domain.payment.dto.request.PreparePaymentRequest;
import com.example.wait4eat.domain.payment.dto.request.RefundPaymentRequest;
import com.example.wait4eat.domain.payment.dto.response.FailPaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.PreparePaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.RefundPaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.SuccessPaymentResponse;
import com.example.wait4eat.domain.payment.service.PaymentService;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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

    @Secured("ROLE_USER")
    @GetMapping("/api/v1/payments/success")
    public ResponseEntity<SuccessResponse<SuccessPaymentResponse>> handlePaymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam(name = "amount") BigDecimal amount
    ) {
        SuccessPaymentResponse response = paymentService.handleSuccess(paymentKey, orderId, amount);
        return ResponseEntity.ok(SuccessResponse.from(response));
    }

    @Secured("ROLE_USER")
    @GetMapping("/api/v1/payments/fail")
    public ResponseEntity<SuccessResponse<FailPaymentResponse>> handlePaymentFail(
            @RequestParam(required = false) String message
    ) {
        String errorMessage = (message != null && !message.isBlank())
                ? message
                : "결제가 실패하거나 취소되었습니다.";

        FailPaymentResponse response = new FailPaymentResponse(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SuccessResponse.from(response));
    }

    @Secured("ROLE_USER")
    @PostMapping("/api/v1/payments/{paymentId}/refund")
    public ResponseEntity<SuccessResponse<RefundPaymentResponse>> refundPayment(
            @PathVariable Long paymentId,
            @RequestBody RefundPaymentRequest request
    ) {
        RefundPaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseEntity.ok(SuccessResponse.from(response));
    }
}