package com.example.wait4eat.scheduler;

import com.example.wait4eat.domain.payment.client.TossPaymentClient;
import com.example.wait4eat.domain.payment.client.dto.TossPaymentData;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.payment.service.PaymentVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentVerifyScheduler {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentVerificationService paymentVerificationService;

    private static final int VERIFY_THRESHOLD_MINUTES = 6;

    @Scheduled(fixedDelay = 5 * 60 * 1000L) // 5분마다 실행
    @Transactional
    public void verifyUnverifiedPayments() {
        long start = System.currentTimeMillis();

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(VERIFY_THRESHOLD_MINUTES);
        List<Payment> targets = paymentRepository.findByVerifiedFalseAndCreatedAtBefore(threshold);

        log.info("[PaymentVerifyScheduler] 결제 정합성 검증 시작");
        log.info("[PaymentVerifyScheduler] 결제 정합성 검증 대상 수: {}", targets.size());

        for (Payment payment : targets) {
            try {
                TossPaymentData paymentData = tossPaymentClient.queryPayment(payment.getPaymentKey());
                paymentVerificationService.verifyTossPaymentData(paymentData);
            } catch (Exception e) {
                log.error("결제 상태 조회 및 정합성 검증 실패: paymentId={}, orderId={}", payment.getId(), payment.getOrderId(), e);
            }
        }

        long end = System.currentTimeMillis();
        log.info("[PaymentVerifyScheduler] 결제 정합성 검증 종료 (소요 시간: {}ms)", end - start);
    }
}
