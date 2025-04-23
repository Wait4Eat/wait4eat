package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.dto.request.UpdateWaitingRequest;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class WaitingServiceConcurrencyTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private EntityManager entityManager;

    private Store store;
    private User owner;
    private User user;
    private Waiting waiting;

    private final int threadCount = 2;
    private ExecutorService executorService;
    private CountDownLatch latch;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .email("owner@example.com")
                .nickname("owner")
                .password("encodedOwnerPassword")
                .role(UserRole.ROLE_OWNER)
                .build();
        owner = userRepository.save(owner);

        store = Store.builder()
                .name("Test Store")
                .user(owner)
                .address("address")
                .openTime(LocalTime.MIN)
                .closeTime(LocalTime.MAX)
                .depositAmount(1000000)
                .build();
        store = storeRepository.save(store);

        user = User.builder()
                .email("user@example.com")
                .nickname("user")
                .role(UserRole.ROLE_USER)
                .password("encodedCustomerPassword")
                .build();
        user = userRepository.save(user);

        waiting = Waiting.builder()
                .store(store)
                .user(user)
                .orderId(UUID.randomUUID().toString())
                .peopleCount(2)
                .myWaitingOrder(0)
                .status(WaitingStatus.WAITING) // 초기 상태를 WAITING으로 설정
                .build();
        waiting = waitingRepository.save(waiting);

        executorService = Executors.newFixedThreadPool(32); // 스레드 풀 크기 설정
        latch = new CountDownLatch(threadCount);
    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    @DisplayName("동시성 테스트 - 하나의 쓰레드에 대해서 취소 및 호출 테스트 ")
    @WithMockAuthUser(userId = 2L, email = "user@example.com", role = UserRole.ROLE_USER)
    void concurrencyPessimisticLockTest() throws InterruptedException {
        Long waitingId = waiting.getId();
        Long userId = waiting.getUser().getId();
        Long storeId = waiting.getStore().getId();

        AtomicInteger cancelSuccessCount = new AtomicInteger(0);
        AtomicInteger callSuccessCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            int threadId = i;
            executorService.execute(() -> {
                try {
                    if (threadId % 2 == 0) { // 짝수 스레드는 취소 시도
                        waitingService.cancelMyWaiting(userId, waitingId);
                        cancelSuccessCount.incrementAndGet();
                    } else { // 홀수 스레드는 호출 시도
                        waitingService.updateWaitingStatus(storeId, waitingId, UpdateWaitingRequest.from(WaitingStatus.CALLED));
                        callSuccessCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    log.error("스레드 {}에서 오류 발생: {}", threadId, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Waiting updatedWaiting = waitingRepository.findById(waitingId).orElseThrow();

        log.info("취소 성공 횟수: {}", cancelSuccessCount.get());
        log.info("호출 성공 횟수: {}", callSuccessCount.get());
        log.info("최종 Waiting 상태: {}", updatedWaiting.getStatus());

        // 비관적 락으로 인해 둘 중 하나의 작업만 성공해야 합니다.
        Assertions.assertTrue(cancelSuccessCount.get() + callSuccessCount.get() <= 1, "둘 중 하나의 작업만 성공해야 합니다.");
        if (cancelSuccessCount.get() == 1) {
            Assertions.assertEquals(WaitingStatus.CANCELLED, updatedWaiting.getStatus());
        } else if (callSuccessCount.get() == 1) {
            Assertions.assertEquals(WaitingStatus.CALLED, updatedWaiting.getStatus());
        } else {
            // 둘 다 실패했거나, 초기 상태 그대로인 경우 (비관적 락이 제대로 작동하지 않았을 가능성)
            Assertions.assertTrue(updatedWaiting.getStatus() == WaitingStatus.WAITING, "초기 상태 또는 CANCELLED/CALLED 상태여야 합니다.");
        }
    }

    @Test
    @DisplayName("동시성 테스트 - 사용자 쓰레드 취소 시도. 사장님 쓰레드 호출 시도")
    @WithMockAuthUser(userId = 2L, email = "user@example.com", role = UserRole.ROLE_USER)
    void concurrencyPessimisticLockUserOwnerTest() throws InterruptedException {
        Long waitingId = waiting.getId();
        Long userId = waiting.getUser().getId();
        Long storeId = waiting.getStore().getId();

        AtomicInteger cancelAttemptCount = new AtomicInteger(0);
        AtomicInteger callAttemptCount = new AtomicInteger(0);
        AtomicInteger cancelSuccessCount = new AtomicInteger(0);
        AtomicInteger callSuccessCount = new AtomicInteger(0);

        // 사용자 스레드 (취소 시도)
        executorService.execute(() -> {
            try {
                for (int i = 0; i < 10; i++) { // 여러 번 취소 시도
                    cancelAttemptCount.incrementAndGet();
                    try {
                        waitingService.cancelMyWaiting(userId, waitingId);
                        cancelSuccessCount.incrementAndGet();
                        // 취소 성공 시 루프 종료 (테스트 목적)
                        break;
                    } catch (Exception e) {
                        log.warn("사용자 취소 시도 중 오류 발생: {}", e.getMessage());
                        try {
                            Thread.sleep(new Random().nextInt(100)); // 약간의 딜레이
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt(); // 인터럽트 상태를 다시 설정
                            return; // 스레드 종료 또는 루프 탈출 등 추가적인 처리 고려
                        }
                    }
                }
            } finally {
                latch.countDown();
            }
        });

        // 사장님 스레드 (호출 시도)
        executorService.execute(() -> {
            try {
                for (int i = 0; i < 10; i++) { // 여러 번 호출 시도
                    callAttemptCount.incrementAndGet();
                    try {
                        waitingService.updateWaitingStatus(storeId, waitingId, UpdateWaitingRequest.from(WaitingStatus.CALLED));
                        callSuccessCount.incrementAndGet();
                        // 호출 성공 시 루프 종료 (테스트 목적)
                        break;
                    } catch (Exception e) {
                        log.warn("사장님 호출 시도 중 오류 발생: {}", e.getMessage());
                        try {
                            Thread.sleep(new Random().nextInt(100)); // 약간의 딜레이
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt(); // 인터럽트 상태를 다시 설정
                            return; // 스레드 종료 또는 루프 탈출 등 추가적인 처리 고려
                        }
                    }
                }
            } finally {
                latch.countDown();
            }
        });

        latch.await();

        Waiting updatedWaiting = waitingRepository.findById(waitingId).orElseThrow();

        log.info("취소 시도 횟수: {}", cancelAttemptCount.get());
        log.info("호출 시도 횟수: {}", callAttemptCount.get());
        log.info("취소 성공 횟수: {}", cancelSuccessCount.get());
        log.info("호출 성공 횟수: {}", callSuccessCount.get());
        log.info("최종 Waiting 상태: {}", updatedWaiting.getStatus());

        // 비관적 락으로 인해 둘 중 하나의 작업만 최종적으로 성공해야 합니다.
        Assertions.assertTrue((cancelSuccessCount.get() == 1 && callSuccessCount.get() == 0) ||
                        (cancelSuccessCount.get() == 0 && callSuccessCount.get() == 1) ||
                        (cancelSuccessCount.get() == 0 && callSuccessCount.get() == 0 && updatedWaiting.getStatus() == WaitingStatus.WAITING),
                "취소 또는 호출 중 하나의 작업만 성공하거나, 초기 상태를 유지해야 합니다.");

        if (cancelSuccessCount.get() == 1) {
            Assertions.assertEquals(WaitingStatus.CANCELLED, updatedWaiting.getStatus());
        } else if (callSuccessCount.get() == 1) {
            Assertions.assertEquals(WaitingStatus.CALLED, updatedWaiting.getStatus());
        }
        // 둘 다 실패한 경우는 초기 상태를 유지해야 함 (비관적 락이 제대로 작동했다면)
    }

}
