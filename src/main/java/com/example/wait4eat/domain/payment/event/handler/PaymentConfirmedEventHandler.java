package com.example.wait4eat.domain.payment.event.handler;

import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.event.PaymentConfirmedEvent;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import com.example.wait4eat.domain.waiting.service.WaitingService;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConfirmedEventHandler {

    private final PaymentRepository paymentRepository;
    private final WaitingService waitingService;
    private final WaitingRepository waitingRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PaymentConfirmedEvent event) {
        Payment payment = paymentRepository.findById(event.getPaymentId())
                .orElseThrow(() -> new CustomException(ExceptionType.PAYMENT_NOT_FOUND));

        // 1. 쿠폰을 사용했다면 사용 처리
        if (payment.getCoupon() != null) {
            payment.getCoupon().markAsUsed();
        }

        // 2. 웨이팅 상태 변경
        Waiting waiting = waitingRepository.findById(event.getWaitingId())
                        .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));
        waitingService.updateWaiting(WaitingStatus.WAITING, waiting);
    }
}
