package com.example.wait4eat.domain.payment.service;

import com.example.wait4eat.domain.payment.dto.request.ConfirmPaymentRequest;
import com.example.wait4eat.domain.payment.dto.request.PreparePaymentRequest;
import com.example.wait4eat.domain.payment.dto.request.RefundPaymentRequest;
import com.example.wait4eat.domain.payment.dto.response.PreparePaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.RefundPaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.SuccessPaymentResponse;
import java.math.BigDecimal;

public interface PaymentService {
    PreparePaymentResponse preparePayment(PreparePaymentRequest request);
    SuccessPaymentResponse confirmPayment(ConfirmPaymentRequest request);
    RefundPaymentResponse refundPayment(Long paymentId, RefundPaymentRequest request);
}