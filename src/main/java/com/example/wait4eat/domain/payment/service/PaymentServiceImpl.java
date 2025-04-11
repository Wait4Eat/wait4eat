package com.example.wait4eat.domain.payment.service;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.coupon.repository.CouponRepository;
import com.example.wait4eat.domain.payment.client.TossPaymentClient;
import com.example.wait4eat.domain.payment.dto.request.PreparePaymentRequest;
import com.example.wait4eat.domain.payment.dto.request.RefundPaymentRequest;
import com.example.wait4eat.domain.payment.dto.response.PreparePaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.RefundPaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.SuccessPaymentResponse;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final WaitingRepository waitingRepository;
    private final CouponRepository couponRepository;

    @Override
    public PreparePaymentResponse preparePayment(PreparePaymentRequest request) {
        Long waitingId = request.getWaitingId();
        Long couponId = request.getCouponId();

        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 웨이팅입니다."));

        String orderId = waiting.getOrderId();
        int originalAmount = waiting.getStore().getDepositAmount();

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰입니다."));

        int discountAmount = coupon.getDiscountAmount().intValue();
        int amount = Math.max(originalAmount - discountAmount, 0);

        // TODO: 현재 로그인 사용자 정보에서 userId 가져오기
        String customerKey = "user-" + waiting.getUser().getId();

        return PreparePaymentResponse.builder()
                .orderId(orderId)
                .originalAmount(BigDecimal.valueOf(originalAmount))
                .amount(BigDecimal.valueOf(amount))
                .customerKey(customerKey)
                .shopName(waiting.getStore().getName())
                .successUrl("http://localhost:3000/payments/success")
                .failUrl("http://localhost:3000/payments/fail")
                .build();
    }

    @Override
    public SuccessPaymentResponse handleSuccess(String paymentKey, String orderId, BigDecimal amount) {
        tossPaymentClient.confirmPayment(paymentKey, orderId, amount);

        Waiting waiting = waitingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 웨이팅입니다."));

        // TODO: orderId로 waiting 조회, payment 저장 등 실제 로직 구현
        return SuccessPaymentResponse.builder()
                .message("결제가 완료되었습니다.")
                .waitingId(1L)
                .paymentId(1L)
                .amount(amount)
                .build();
    }

    @Override
    public RefundPaymentResponse refundPayment(Long paymentId, RefundPaymentRequest request) {
        // TODO: 환불 처리 로직
        return null;
    }
}