package com.example.wait4eat.domain.payment.service;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.coupon.repository.CouponRepository;
import com.example.wait4eat.domain.payment.client.TossPaymentClient;
import com.example.wait4eat.domain.payment.dto.request.PreparePaymentRequest;
import com.example.wait4eat.domain.payment.dto.request.RefundPaymentRequest;
import com.example.wait4eat.domain.payment.dto.response.PreparePaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.RefundPaymentResponse;
import com.example.wait4eat.domain.payment.dto.response.SuccessPaymentResponse;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final WaitingRepository waitingRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    public PreparePaymentResponse preparePayment(PreparePaymentRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인 사용자 정보를 찾을 수 없습니다."));

        Long waitingId = request.getWaitingId();
        Long couponId = request.getCouponId();

        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 웨이팅입니다."));

        if (!waiting.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("해당 웨이팅은 현재 로그인한 사용자의 것이 아닙니다.");
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰입니다."));

        String orderId = waiting.getOrderId();
        int originalAmount = waiting.getStore().getDepositAmount();
        int discountAmount = coupon.getDiscountAmount().intValue();
        int amount = Math.max(originalAmount - discountAmount, 0);

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
    @Transactional
    public SuccessPaymentResponse handleSuccess(String paymentKey, String orderId, BigDecimal amount) {
        tossPaymentClient.confirmPayment(paymentKey, orderId, amount);

        Waiting waiting = waitingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 웨이팅입니다."));

        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .amount(amount)
                .user(waiting.getUser())
                .waiting(waiting)
                .status(PaymentStatus.PAID)
                .build();

        paymentRepository.save(payment);

        return SuccessPaymentResponse.builder()
                .message("결제가 완료되었습니다.")
                .waitingId(waiting.getId())
                .paymentId(payment.getId())
                .amount(amount)
                .build();
    }

    @Override
    @Transactional
    public RefundPaymentResponse refundPayment(Long paymentId, RefundPaymentRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다."));
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("환불 가능한 상태가 아닙니다.");
        }
        payment.getRefundedAt();
        paymentRepository.save(payment);
        
        return RefundPaymentResponse.builder()
                .message("환불이 완료되었습니다.")
                .refundedAt(LocalDateTime.now().toString())
                .build();
    }
}