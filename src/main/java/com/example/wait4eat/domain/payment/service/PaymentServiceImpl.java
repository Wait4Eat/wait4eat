package com.example.wait4eat.domain.payment.service;

import com.example.wait4eat.client.SlackNotificationService;
import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.payment.client.dto.TossCancelPaymentResponse;
import com.example.wait4eat.domain.payment.client.dto.TossConfirmPaymentResponse;
import com.example.wait4eat.domain.payment.client.exception.TossPaymentErrorException;
import com.example.wait4eat.domain.payment.dto.request.ConfirmPaymentRequest;
import com.example.wait4eat.domain.payment.entity.PrePayment;
import com.example.wait4eat.domain.payment.enums.PrePaymentStatus;
import com.example.wait4eat.domain.payment.event.PaymentConfirmedEvent;
import com.example.wait4eat.domain.payment.event.PaymentRefundedEvent;
import com.example.wait4eat.domain.payment.repository.PrePaymentRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.coupon.repository.CouponRepository;
import com.example.wait4eat.domain.payment.client.TossPaymentClient;
import com.example.wait4eat.domain.payment.dto.request.PreparePaymentRequest;
import com.example.wait4eat.domain.payment.dto.response.PreparePaymentResponse;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final WaitingRepository waitingRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final PrePaymentRepository prePaymentRepository;
    private final UserRepository userRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ApplicationEventPublisher eventPublisher;
    private final SlackNotificationService slackNotificationService;


    @Override
    @Transactional
    public PreparePaymentResponse preparePayment(PreparePaymentRequest request) {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(authUser.getEmail())
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));

        log.info("[preparePayment] userId={}, email={}", user.getId(), user.getEmail());


        // TODO : 유효한 웨이팅인지 확인 필요
        Waiting waiting = waitingRepository.findById(request.getWaitingId())
                .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

        if (!waiting.getUser().getId().equals(user.getId())) {
            throw new CustomException(ExceptionType.NO_PERMISSION_ACTION, "웨이팅 당사자만 결제가 가능합니다.");
        }

        // TODO : 유효한 쿠폰인지 확인 필요
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new CustomException(ExceptionType.COUPON_NOT_FOUND));

        int originalAmount = waiting.getStore().getDepositAmount();
        int discountAmount = coupon.getDiscountAmount().intValue();
        int amount = Math.max(originalAmount - discountAmount, 0);

        // TODO : 같은 orderId를 가진 REQUESTED 상태의 PrePayment가 있는지 확인 필요
        PrePayment prePayment = prePaymentRepository.save(
                PrePayment.builder()
                        .user(user)
                        .waiting(waiting)
                        .coupon(coupon)
                        .orderId(waiting.getOrderId())
                        .originalAmount(BigDecimal.valueOf(originalAmount))
                        .amount(BigDecimal.valueOf(amount))
                        .status(PrePaymentStatus.REQUESTED)
                        .build()
        );

        log.info("[preparePayment] PrePayment saved: prePaymentId={}, orderId={}", prePayment.getId(), prePayment.getOrderId());

        return PreparePaymentResponse.from(prePayment);
    }

    @Override
    @Transactional
    public SuccessPaymentResponse confirmPayment(ConfirmPaymentRequest request) {
        String orderId = request.getOrderId();
        String paymentKey = request.getPaymentKey();
        BigDecimal amount = request.getAmount();

        log.info("[confirmPayment] start confirmation: orderId={}, paymentKey={}, amount={}", orderId, paymentKey, amount);

        PrePayment prePayment = prePaymentRepository.findByOrderIdAndStatus(orderId, PrePaymentStatus.REQUESTED)
                .orElseThrow(() -> new CustomException(ExceptionType.PREPAYMENT_DOES_NOT_EXIST));

        // 가주문 데이터와 일치하는지 확인
        if (amount == null || prePayment.getAmount() == null || prePayment.getAmount().compareTo(amount) != 0) {
            log.warn("[confirmPayment] payment amount mismatch: orderId={}, expected={}, actual={}",
                    orderId, prePayment.getAmount(), amount);
            throw new CustomException(ExceptionType.PREPAYMENT_AMOUNT_MISMATCH);
        }

        try {
            TossConfirmPaymentResponse successResponse = tossPaymentClient.confirmPayment(paymentKey, orderId, amount);
            log.info("[confirmPayment] toss confirmation successfully: paymentKey={}, orderId={}, totalAmount={}",
                    successResponse.getPaymentKey(), successResponse.getOrderId(), successResponse.getTotalAmount());

            Payment payment = paymentRepository.save(
                    Payment.builder()
                            .paymentKey(successResponse.getPaymentKey())
                            .orderId(successResponse.getOrderId())
                            .coupon(prePayment.getCoupon())
                            .user(prePayment.getUser())
                            .waiting(prePayment.getWaiting())
                            .originalAmount(prePayment.getOriginalAmount())
                            .amount(successResponse.getTotalAmount())
                            .status(PaymentStatus.SUCCEEDED)
                            .paidAt(successResponse.getApprovedAt().toLocalDateTime())
                            .build()
            );

            prePayment.markAsCompleted();
            log.info("[confirmPayment] Payment saved: paymentId={}, status=SUCCEEDED", payment.getId());

            eventPublisher.publishEvent(PaymentConfirmedEvent.of(payment, payment.getWaiting()));

            return SuccessPaymentResponse.from(payment);
        } catch (TossPaymentErrorException e) {
            log.warn("[confirmPayment] toss confirmation failed: orderId={}, paymentKey={}, reason={}",
                    orderId, paymentKey, e.getMessage());

            paymentRepository.save(
                    Payment.builder()
                            .paymentKey(paymentKey)
                            .orderId(orderId)
                            .user(prePayment.getUser())
                            .waiting(prePayment.getWaiting())
                            .originalAmount(prePayment.getOriginalAmount())
                            .amount(amount)
                            .status(PaymentStatus.FAILED)
                            .failedAt(LocalDateTime.now())
                            .build()
            );

            prePayment.markAsFailed();

            throw new CustomException(ExceptionType.PAYMENT_CONFIRM_FAILED, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void refundPayment(Long paymentId, String refundReason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ExceptionType.PAYMENT_NOT_FOUND));

        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            log.warn("[refundPayment] invalid payment status for refund: paymentId={}, status={} ",
                    paymentId, payment.getStatus());
            throw new IllegalArgumentException("환불 가능한 상태가 아닙니다.");
        }

        try {
            TossCancelPaymentResponse response = tossPaymentClient.cancelPayment(payment.getPaymentKey(), refundReason);

            payment.markAsRefunded(response.getCancels().get(0).getCanceledAt().toLocalDateTime());
            log.info("[refundPayment] Payment refunded successfully: paymentId={}, canceledAt={}",
                    payment.getId(), payment.getRefundedAt());

            eventPublisher.publishEvent(PaymentRefundedEvent.from(payment));
        } catch (TossPaymentErrorException e) {
            log.warn("[refundPayment] toss cancel failed: paymentId={}, paymentKey={}, reason={}",
                    paymentId, payment.getPaymentKey(), e.getMessage());

            payment.markAsRefundFailed();
            slackNotificationService.sendNotificationToSlack(
                    String.format(
                            "[환불 실패] paymentId=%d, paymentKey=%s, 이유=%s",
                            payment.getId(),
                            payment.getPaymentKey(),
                            e.getMessage()
                    )
            );
            throw new CustomException(ExceptionType.PAYMENT_CONFIRM_FAILED, e.getMessage());
        }
    }
}
