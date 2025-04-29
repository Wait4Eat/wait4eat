package com.example.wait4eat.domain.payment.validator;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.entity.PrePayment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.payment.enums.PrePaymentStatus;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    public void validatePreparePayment(
            User user,
            Waiting waiting,
            Coupon coupon,
            Optional<PrePayment> optionalPrePayment
    ) {
       if (!waiting.getUser().getId().equals(user.getId())) {
           throw new CustomException(ExceptionType.NO_PERMISSION_ACTION, "웨이팅 당사자만 결제 가능합니다.");
       }
       if (!waiting.getStatus().equals(WaitingStatus.REQUESTED)) {
           throw new CustomException(ExceptionType.INVALID_PREPAYMENT_REQUEST, "결제가 가능한 웨이팅 상태가 아닙니다.");
       }
       if (coupon.getIsUsed()) {
            throw new CustomException(ExceptionType.COUPON_ALREADY_USED);
       }
       if (optionalPrePayment.isPresent()) {
           PrePayment prePayment = optionalPrePayment.get();
           if (prePayment.getStatus() != PrePaymentStatus.REQUESTED) {
               throw new CustomException(ExceptionType.INVALID_PREPAYMENT_REQUEST,
                       "해당 웨이팅으로 이미 승인되거나 실패한 결제 내역이 존재합니다."
               );
           }
       }
    }

    public void validateConfirmPayment(PrePayment prePayment, BigDecimal requestedAmount, boolean paymentAlreadyExists) {
        if (paymentAlreadyExists) {
            throw new CustomException(ExceptionType.INVALID_PAYMENT_REQUEST, "이미 결제가 완료된 주문입니다.");
        }
        if (prePayment.getStatus() != PrePaymentStatus.REQUESTED) {
            throw new CustomException(ExceptionType.INVALID_PAYMENT_REQUEST, "결제 가능한 상태가 아닙니다.");
        }
        if (requestedAmount == null || prePayment.getAmount() == null || prePayment.getAmount().compareTo(requestedAmount) != 0) {
            throw new CustomException(ExceptionType.PREPAYMENT_AMOUNT_MISMATCH);
        }
    }

    public void validateRefundPayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new CustomException(ExceptionType.INVALID_REFUND_REQUEST, "환불 가능한 상태가 아닙니다.");
        }
    }
}
