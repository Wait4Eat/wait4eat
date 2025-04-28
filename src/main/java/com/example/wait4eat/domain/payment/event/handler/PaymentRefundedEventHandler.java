package com.example.wait4eat.domain.payment.event.handler;

import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.event.PaymentRefundedEvent;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentRefundedEventHandler {

    private final PaymentRepository paymentRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PaymentRefundedEvent event) {
        Payment payment = paymentRepository.findById(event.getPaymentId())
                .orElseThrow(() -> new CustomException(ExceptionType.PAYMENT_NOT_FOUND));

        // 1. 쿠폰을 사용했다면 복구 처리
        if (payment.getCoupon() != null) {
            payment.getCoupon().markAsNotUsed();
        }

        // 결제 취소 완료 알림 전송
    }
}
