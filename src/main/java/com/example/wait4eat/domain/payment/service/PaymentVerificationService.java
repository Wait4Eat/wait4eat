package com.example.wait4eat.domain.payment.service;

import com.example.wait4eat.client.SlackNotificationService;
import com.example.wait4eat.domain.payment.client.dto.TossPaymentData;
import com.example.wait4eat.domain.payment.dto.request.TossWebhookPayload;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.entity.PrePayment;
import com.example.wait4eat.domain.payment.enums.PrePaymentStatus;
import com.example.wait4eat.domain.payment.enums.TossPaymentStatus;
import com.example.wait4eat.domain.payment.event.PaymentRefundRequestedEvent;
import com.example.wait4eat.domain.payment.event.PaymentRefundedEvent;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.payment.repository.PrePaymentRepository;
import com.example.wait4eat.global.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVerificationService {

    private final PaymentRepository paymentRepository;
    private final PrePaymentRepository prePaymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SlackNotificationService slackNotificationService;

    public void verifyFromWebhook(TossWebhookPayload payload) {
        log.info("[Webhook 수신] eventType = {}", payload.getEventType());
        verifyTossPaymentData(payload.getData());
    }

    @Transactional
    public void verifyTossPaymentData(TossPaymentData paymentData) {
        TossPaymentStatus tossPaymentStatus = paymentData.getPaymentStatus();
        String orderId = paymentData.getOrderId();
        log.info("[Toss Payment Data 수신] orderId={}, status={}", orderId, tossPaymentStatus);

        PrePayment prePayment = prePaymentRepository.findByOrderId(orderId).orElse(null);
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        switch (tossPaymentStatus) {
            case DONE -> handleDone(paymentData, prePayment, payment);
            case ABORTED -> handleAborted(paymentData, prePayment, payment);
            case EXPIRED -> handleExpired(paymentData, prePayment, payment);
            case CANCELED -> handleCanceled(paymentData, prePayment, payment);
            default -> notifySlackUnexpectedStatus(paymentData, prePayment, payment);
        }
    }

    private void handleDone(TossPaymentData payload, PrePayment prePayment, Payment payment) {
        // 1. 정상 성공 시나리오
        if (payment != null && payment.isPaid()) {
            log.info("결제 성공 검증 완료: paymentId={}", payment.getId());
            payment.markVerified();
            return;
        }
        // 2. 결제 승인 API 호출 후 DB 반영 실패
        if (payment == null && prePayment.getStatus().equals(PrePaymentStatus.REQUESTED)) {
            log.warn("결제 승인되었지만 DB 저장 실패 - 환불 요청 예정: orderId={}", payload.getOrderId());
            payment.markAsSucceeded();
            // 결제 취소 이벤트 발행
            eventPublisher.publishEvent(PaymentRefundRequestedEvent.of(
                    payment.getWaiting(), "결제 승인 후 서버 에러 발생")
            );
        }
        // 3. 기타 비정상 케이스
        notifySlackUnexpectedStatus(payload, prePayment, payment);
    }

    private void handleExpired(TossPaymentData payload, PrePayment prePayment, Payment payment) {
        // 1. 정상 만료 시나리오 (가결제만 생성 후 결제 승인 요청 X)
        if (payment == null && prePayment.getStatus().equals(PrePaymentStatus.REQUESTED)) {
            log.info("만료 처리 완료: prePaymentId={}", prePayment.getId());
            prePayment.markAsExpired();
            return;
        }
        // 2. 스케줄러에 의해 Expired 마킹된 상태
        if (payment == null && prePayment.getStatus().equals(PrePaymentStatus.EXPIRED)) {
            log.info("이미 만료 처리된 상태입니다: prePaymentId={}", prePayment.getId());
            return;
        }
        // 3. 기타 비정상 케이스
        notifySlackUnexpectedStatus(payload, prePayment, payment);
    }

    private void handleAborted(TossPaymentData payload, PrePayment prePayment, Payment payment) {
        // 1. 정상 실패 시나리오
        if (payment != null && payment.isFailed()) {
            log.info("결제 실패 검증 완료: paymentId={}", payment.getId());
            payment.markVerified();
            return;
        }
        // 2. 결제 승인 API 호출 후 DB 반영 실패
        if (payment == null && prePayment.getStatus().equals(PrePaymentStatus.REQUESTED)) {
            log.warn("결제 승인 실패 후 DB 반영 실패 - 실패 처리 수행: orderId={}", payload.getOrderId());
            prePayment.markAsFailed();
            return;
        }
        // 3. 기타 비정상 케이스
        notifySlackUnexpectedStatus(payload, prePayment, payment);
    }

    private void handleCanceled(TossPaymentData payload, PrePayment prePayment, Payment payment) {
        // 1. 정상 환불 시나리오
        if (payment != null && payment.isRefunded()) {
            log.info("환불 완료 검증 처리: paymentId={}", payment.getId());
            payment.markVerified();
            return;
        }
        // 2. 결제 취소 API 호출 후 DB 반영 실패
        if (payment != null && payment.isPaid()) {
            log.warn("환불 완료되었지만 DB 반영 실패 - 환불 처리 수행: orderId={}", payload.getOrderId());
            TossPaymentData.CancelInfo cancelInfo = payload.getCancels().get(0);
            payment.markAsRefunded(DateTimeUtils.toKstLocalDateTime(cancelInfo.getCanceledAt()));
            eventPublisher.publishEvent(PaymentRefundedEvent.from(payment));
            return;
        }
        // 3. 기타 비정상 케이스
        notifySlackUnexpectedStatus(payload, prePayment, payment);
    }

    private void notifySlackUnexpectedStatus(TossPaymentData payload, PrePayment prePayment, Payment payment) {
        slackNotificationService.sendUnexpectedWebhookStatusNotification(
                payload.getOrderId(),
                payload.getPaymentStatus().name(),
                prePayment != null ? prePayment.getStatus().name() : null,
                payment != null ? payment.getStatus().name() : null
        );
    }
}
