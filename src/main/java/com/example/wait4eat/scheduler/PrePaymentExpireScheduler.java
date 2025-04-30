package com.example.wait4eat.scheduler;

import com.example.wait4eat.domain.payment.entity.PrePayment;
import com.example.wait4eat.domain.payment.enums.PrePaymentStatus;
import com.example.wait4eat.domain.payment.repository.PrePaymentRepository;
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
public class PrePaymentExpireScheduler {

    private final PrePaymentRepository prePaymentRepository;
    private static final int EXPIRE_THRESHOLD_MINUTES = 11;

    @Scheduled(fixedDelay = 5 * 60 * 1000L) // 5분마다 실행
    @Transactional
    public void expireRequestedPrePayments() {
        long start = System.currentTimeMillis();
        log.info("[PrePaymentExpireScheduler] 만료 처리 시작");

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(EXPIRE_THRESHOLD_MINUTES);
        List<PrePayment> targets = prePaymentRepository
                .findByStatusAndCreatedAtBefore(PrePaymentStatus.REQUESTED, threshold);

        log.info("[PrePaymentExpireScheduler] 만료 처리 대상 수: {}", targets.size());

        for (PrePayment prePayment : targets) {
            prePayment.markAsExpired();
            log.info("만료 처리됨: prePaymentId={}, orderId={}", prePayment.getId(), prePayment.getOrderId());
        }

        long end = System.currentTimeMillis();
        log.info("[PrePaymentExpireScheduler] 만료 처리 완료 (소요 시간: {}ms)", end - start);
    }
}
