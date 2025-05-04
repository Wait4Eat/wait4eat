package com.example.wait4eat.domain.dashboard;

import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.coupon.repository.CouponRepository;
import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import com.example.wait4eat.domain.coupon_event.repository.CouponEventRepository;
import com.example.wait4eat.domain.payment.entity.Payment;
import com.example.wait4eat.domain.payment.enums.PaymentStatus;
import com.example.wait4eat.domain.payment.repository.PaymentRepository;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
public class TestDataSetup {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private CouponEventRepository couponEventRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void 대용량_데이터_셋업() {
        List<User> users = new ArrayList<>();

        // USER 1500명
        for (int i = 0; i < 1500; i++) {
            User user = User.builder()
                    .email("user" + (i + 1) + "@test.com")
                    .nickname("user" + (i + 1))
                    .password("password")
                    .role(UserRole.ROLE_USER)
                    .build();

            if (i % 2 == 0) {
                user.setLastLoginDate(LocalDate.now().minusDays(1));
            }

            users.add(user);
        }

        // OWNER 1500명
        for (int i = 0; i < 1500; i++) {
            User owner = User.builder()
                    .email("owner" + (i + 1) + "@test.com")
                    .nickname("owner" + (i + 1))
                    .password("password")
                    .role(UserRole.ROLE_OWNER)
                    .build();

            owner.setLastLoginDate(LocalDate.now().minusDays(1));
            users.add(owner);
        }
        userRepository.saveAll(users);

        // 스토어 1500개
        List<Store> stores = new ArrayList<>();

        for (int i = 0; i < 1500; i++) {
            Store store = Store.builder()
                    .user(users.get(i + 1000))
                    .name("store" + (i + 1))
                    .address("address")
                    .openTime(LocalTime.of(9, 0))
                    .closeTime(LocalTime.of(21, 0))
                    .depositAmount(1000)
                    .build();

            stores.add(store);
        }
        storeRepository.saveAll(stores);

        // waiting
        List<Waiting> waitings = new ArrayList<>();

        // 인기가게 1
        for (int i = 0; i < 500; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(1400))
                    .user(users.get(i))
                    .orderId("store" + stores.get(0).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 2
        for (int i = 0; i < 400; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(1))
                    .user(users.get(i+500))
                    .orderId("store" + stores.get(1).getId() + "_user" + users.get(i).getId() + "_order" + (i + 501))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 3
        for (int i = 0; i < 300; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(2))
                    .user(users.get(i + 900))
                    .orderId("store" + stores.get(2).getId() + "_user" + users.get(i).getId() + "_order" + (i + 901))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 4
        for (int i = 0; i < 100; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(3))
                    .user(users.get(i+1200))
                    .orderId("store" + stores.get(3).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1201))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 5
        for (int i = 0; i < 90; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(4))
                    .user(users.get(i+1300))
                    .orderId("store" + stores.get(4).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1301))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 6
        for (int i = 0; i < 50; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(5))
                    .user(users.get(i+1390))
                    .orderId("store" + stores.get(5).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1391))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 7
        for (int i = 0; i < 20; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(6))
                    .user(users.get(i+1420))
                    .orderId("store" + stores.get(6).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1421))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 8
        for (int i = 0; i < 15; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(7))
                    .user(users.get(i+1440))
                    .orderId("store" + stores.get(7).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1441))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 9
        for (int i = 0; i < 10; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(8))
                    .user(users.get(i + 1455))
                    .orderId("store" + stores.get(8).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1456))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 인기가게 10
        for (int i = 0; i < 10; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(9))
                    .user(users.get(i+1465))
                    .orderId("store" + stores.get(9).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1466))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }

        // 나머지
        for (int i = 1474; i < 1500; i++) {
            Waiting waiting = Waiting.builder()
                    .store(stores.get(i))
                    .user(users.get(i))
                    .orderId("store" + stores.get(i).getId() + "_user" + users.get(i).getId() + "_order" + (i + 1))
                    .peopleCount(1)
                    .status(WaitingStatus.COMPLETED)
                    .build();

            waitings.add(waiting);
        }
        waitingRepository.saveAll(waitings);

        // 쿠폰 이벤트
        CouponEvent couponEvent = CouponEvent.builder()
                .store(stores.get(0))
                .name("CouponEvent")
                .discountAmount(BigDecimal.valueOf(1000))
                .totalQuantity(1500)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        couponEventRepository.save(couponEvent);

        // 쿠폰
        Coupon coupon = Coupon.builder()
                .user(users.get(1))
                .couponEvent(couponEvent)
                .discountAmount(BigDecimal.valueOf(1000))
                .expiresAt(LocalDateTime.now().plusDays(1))
                .issuedAt(LocalDateTime.now())
                .build();
        couponRepository.save(coupon);

        // 결제
        List<Payment> payments = new ArrayList<>();

        for (int i = 0; i < 1500; i++) {
            Payment payment = Payment.builder()
                    .user(waitings.get(i).getUser())
                    .waiting(waitings.get(i))
                    .coupon(coupon)
                    .orderId(waitings.get(i).toString())
                    .paymentKey("paymentKey")
                    .originalAmount(BigDecimal.valueOf(5000))
                    .amount(BigDecimal.valueOf(5000).subtract(coupon.getDiscountAmount()))
                    .status(PaymentStatus.SUCCEEDED)
                    .paidAt(LocalDateTime.now().minusDays(1))
                    .build();

            payments.add(payment);
        }
        paymentRepository.saveAll(payments);
    }
}
