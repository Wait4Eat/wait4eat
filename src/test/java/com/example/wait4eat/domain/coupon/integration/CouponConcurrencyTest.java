package com.example.wait4eat.domain.coupon.integration;

import com.example.wait4eat.domain.auth.dto.request.SignupRequest;
import com.example.wait4eat.domain.coupon.entity.Coupon;
import com.example.wait4eat.domain.coupon.repository.CouponRepository;
import com.example.wait4eat.domain.coupon.service.CouponService;
import com.example.wait4eat.domain.coupon_event.entity.CouponEvent;
import com.example.wait4eat.domain.coupon_event.repository.CouponEventRepository;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.auth.dto.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class CouponConcurrencyTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponEventRepository couponEventRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long couponEventId;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        // 1. 테스트 유저 1000명 생성
        testUsers = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            SignupRequest request = SignupRequest.builder()
                    .email("test" + i + "@example.com")
                    .password("Test1234!")
                    .nickname("tester" + i)
                    .role("ROLE_USER")
                    .build();

            String encoded = passwordEncoder.encode(request.getPassword());

            User user = User.builder()
                    .email(request.getEmail())
                    .nickname(request.getNickname())
                    .password(encoded)
                    .role(request.getUserRole())
                    .build();

            userRepository.save(user);
            testUsers.add(user);
        }

        // 2. 테스트 사장 1명 생성
        User storeOwner = User.builder()
                .email("owner@example.com")
                .nickname("김사장")
                .password(passwordEncoder.encode("Test1234!"))
                .role(UserRole.valueOf("ROLE_OWNER"))
                .build();

        userRepository.save(storeOwner);

        // 3. 테스트 사장의 스토어 생성
        Store store = Store.builder()
                .user(storeOwner)
                .name("테스트 가게")
                .address("서울시 테스트구 테스트동")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .description("테스트용 가게입니다.")
                .depositAmount(10000)
                .build();

        storeRepository.save(store);

        // 4. 테스트 스토어의 쿠폰이벤트 생성(발급 수량 1개로 지정)
        CouponEvent couponEvent = CouponEvent.builder()
                .store(store)
                .name("테스트 쿠폰")
                .discountAmount(BigDecimal.valueOf(2000))
                .totalQuantity(1)
                .issuedQuantity(0)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        couponEventRepository.save(couponEvent);
        couponEventId = couponEvent.getId();
    }

    @Test
    void 천명의_유저가_동시에_쿠폰을_요청하면_1명만_성공하고_작업_결과가_일치해야_한다() throws InterruptedException {
        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> successNicknames = Collections.synchronizedList(new ArrayList<>());
        List<String> failNicknames = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            int userIndex = i;
            executorService.submit(() -> {
                try {
                    User user = testUsers.get(userIndex);
                    AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), user.getRole());
                    couponService.createCoupon(authUser, couponEventId);
                    System.out.println("O 쿠폰 발급 성공: " + user.getNickname());
                    successCount.incrementAndGet();
                    successNicknames.add(user.getNickname());
                } catch (Exception e) {
                    System.out.println("X 쿠폰 발급 실패: " + testUsers.get(userIndex).getNickname());
                    failCount.incrementAndGet();
                    failNicknames.add(testUsers.get(userIndex).getNickname());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        List<Coupon> issuedCoupons = couponRepository.findAll();
        CouponEvent event = couponEventRepository.findById(couponEventId).orElseThrow();

        // 핵심 검증
        assertEquals(1, issuedCoupons.size(), "쿠폰은 정확히 1명에게만 발급되어야 합니다");
        assertEquals(1, successCount.get(), "성공적으로 쿠폰을 받은 사용자는 1명이어야 합니다");
        assertEquals(1, event.getIssuedQuantity(), "쿠폰 이벤트의 발급 수량도 1이어야 합니다");

        int totalAttempts = successCount.get() + failCount.get();
        assertEquals(threadCount, totalAttempts, "전체 작업 수는 성공 + 실패 수와 같아야 합니다");

        // 로그 출력
        System.out.println("O 쿠폰 발급 성공 사용자 닉네임: " + successNicknames);
        System.out.println("X 쿠폰 발급 실패 사용자 닉네임: " + failNicknames);
        System.out.println("총 스레드 작업 수: " + threadCount + ", 쿠폰 발급자 수: " + successCount.get() + ", 쿠폰 미발급자 수: " + failCount.get());
    }
}