package com.example.wait4eat.domain.payment.service;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.payment.consts.PaymentEndpoint;
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
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TossPaymentClient tossPaymentClient;
    private final WaitingRepository waitingRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PreparePaymentResponse preparePayment(PreparePaymentRequest request) {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));

        Waiting waiting = waitingRepository.findById(request.getWaitingId())
                .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

        if (!waiting.getUser().getId().equals(user.getId())) {
            throw new CustomException(ExceptionType.NO_PERMISSION_ACTION, "웨이팅 당사자만 결제가 가능합니다.");
        }

        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new CustomException(ExceptionType.COUPON_NOT_FOUND));

        int originalAmount = waiting.getStore().getDepositAmount();
        int discountAmount = coupon.getDiscountAmount().intValue();
        int amount = Math.max(originalAmount - discountAmount, 0);

        return PreparePaymentResponse.builder()
                .orderId(waiting.getOrderId())
                .originalAmount(BigDecimal.valueOf(originalAmount))
                .amount(BigDecimal.valueOf(amount))
                .customerKey("user-" + user.getId())
                .shopName(waiting.getStore().getName())
                .successPath(PaymentEndpoint.SUCCESS_PATH)
                .failPath(PaymentEndpoint.FAIL_PATH)
                .build();
    }

    @Override
    @Transactional
    public SuccessPaymentResponse handleSuccess(String paymentKey, String orderId, BigDecimal amount) {
        try {
            tossPaymentClient.confirmPayment(paymentKey, orderId, amount);
        } catch (Exception e) {
            String message = e.getMessage();

            if (message != null && message.contains("ALREADY_PROCESSED_PAYMENT")) {
                log.warn("Toss confirm skipped: 이미 처리된 결제입니다.");
            } else {
                log.error("Toss confirm 실패: {}", message);
                throw e;
            }
        }

        Waiting waiting = waitingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 웨이팅입니다."));

        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .amount(amount)
                .originalAmount(BigDecimal.valueOf(waiting.getStore().getDepositAmount()))
                .user(waiting.getUser())
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
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
        payment.markAsRefunded();
        paymentRepository.save(payment);

        return RefundPaymentResponse.builder()
                .message("환불이 완료되었습니다.")
                .refundedAt(LocalDateTime.now().toString())
                .build();
    }
}
