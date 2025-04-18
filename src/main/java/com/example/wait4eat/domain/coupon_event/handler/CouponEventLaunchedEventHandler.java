package com.example.wait4eat.domain.coupon_event.handler;

import com.example.wait4eat.domain.coupon_event.event.CouponEventLaunchedEvent;
import com.example.wait4eat.domain.notification.service.NotificationDispatchService;
import com.example.wait4eat.domain.storewishlist.repository.StoreWishlistRepository;
import com.example.wait4eat.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventLaunchedEventHandler {

    private final StoreWishlistRepository storeWishlistRepository;
    private final NotificationDispatchService dispatchService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CouponEventLaunchedEvent event) {
        List<User> users = storeWishlistRepository.findAllUsersByStoreId(event.getStoreId());

        for (User user : users) {
            dispatchService.send(user.getId(), event.getStoreName());
        }
    }
}
