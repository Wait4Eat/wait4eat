package com.example.wait4eat.domain.payment.controller;

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

    @GetMapping("/api/v1/payments/success")
    public ResponseEntity<SuccessResponse<SuccessPaymentResponse>> handlePaymentSuccess(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam(name = "amount") BigDecimal amount
    ) {
        SuccessPaymentResponse response = paymentService.handleSuccess(paymentKey, orderId, amount);
        return ResponseEntity.ok(SuccessResponse.from(response));
    }

    @GetMapping("/api/v1/payments/fail")
    public ResponseEntity<ErrorResponse> handlePaymentFail(
            @RequestParam(required = false) String message
    ) {
        String errorMessage = (message != null && !message.isBlank())
                ? message
                : ExceptionType.INTERNAL_SERVER_ERROR.getMessage();

        CustomException customException = new CustomException(ExceptionType.INTERNAL_SERVER_ERROR, errorMessage);

        return ResponseEntity
                .status(customException.getHttpStatus())
                .body(ErrorResponse.from(customException));
    }

    @PostMapping("/api/v1/payments/{paymentId}/refund")
    public ResponseEntity<SuccessResponse<RefundPaymentResponse>> refundPayment(
            @PathVariable Long paymentId,
            @RequestBody RefundPaymentRequest request
    ) {
        RefundPaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseEntity.ok(SuccessResponse.from(response));
    }
}