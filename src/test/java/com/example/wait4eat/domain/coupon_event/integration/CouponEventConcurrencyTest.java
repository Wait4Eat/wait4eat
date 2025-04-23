package com.example.wait4eat.domain.coupon_event.integration;

import com.example.wait4eat.domain.coupon_event.dto.request.CreateCouponEventRequest;
import com.example.wait4eat.domain.coupon_event.repository.CouponEventRepository;
import com.example.wait4eat.domain.coupon_event.service.CouponEventService;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class CouponEventConcurrencyTest {

    @Autowired
    private CouponEventService couponEventService;

    @Autowired
    private CouponEventRepository couponEventRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long storeId;
    private AuthUser authUser;

    @BeforeEach
    void setUp() {
        // 1. 테스트 사장 1명 생성
        User storeOwner = User.builder()
                .email("owner@example.com")
                .nickname("김사장")
                .password(passwordEncoder.encode("Test1234!"))
                .role(UserRole.valueOf("ROLE_OWNER"))
                .build();

        userRepository.save(storeOwner);

        // 2. 테스트 사장의 스토어 생성
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
        storeId = store.getId();

        authUser = new AuthUser(storeOwner.getId(), storeOwner.getEmail(), storeOwner.getRole());
    }

    @Test
    void 한_명의_사장이_동시에_여러_번_쿠폰이벤트를_생성해도_오직_하나만_성공해야_한다() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    CreateCouponEventRequest request = new CreateCouponEventRequest();
                    request.setName("테스트 쿠폰");
                    request.setDiscountAmount(BigDecimal.valueOf(2000));
                    request.setTotalQuantity(10);
                    request.setExpiresAt(LocalDateTime.now().plusDays(1));

                    couponEventService.createCouponEvent(authUser, storeId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        long totalCreated = couponEventRepository.count();

        assertEquals(1, totalCreated, "쿠폰이벤트는 1개만 생성되어야 합니다");
        assertEquals(1, successCount.get(), "성공한 요청은 정확히 1개여야 합니다");
        assertEquals(threadCount - 1, failCount.get(), "나머지는 실패해야 합니다");

        System.out.println("총 생성된 쿠폰이벤트 수: " + totalCreated);
        System.out.println("총 스레드 작업 수: " + threadCount + ", 성공 요청 수: " + successCount.get() + ", 실패 요청 수: " + failCount.get());
    }
}