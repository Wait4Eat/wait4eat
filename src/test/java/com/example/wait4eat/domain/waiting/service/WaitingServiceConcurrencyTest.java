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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalTime;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

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
    private RedisTemplate<String, Long> waitingIdRedisTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate; // 트랜잭션 관리를 위한 템플릿 빈 주입

    private Store store;
    private User owner;
    private User user1;

    private static final String WAITING_QUEUE_KEY_PREFIX = "waiting:queue:store:";
    private static final int repetitionCount = 10;

    @BeforeEach
    void setUpBase() {
        owner = createUser("owner" + System.currentTimeMillis() + "@example.com", "owner", UserRole.ROLE_OWNER);
        user1 = createUser("user1" + System.currentTimeMillis() + "@example.com", "user1", UserRole.ROLE_USER);
        store = createStore(owner, "Test Store");
    }

    private User createUser(String email, String nickname, UserRole role) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .password("encodedPassword")
                .role(role)
                .build();
        return userRepository.save(user);
    }

    private Store createStore(User owner, String name) {
        Store store = Store.builder()
                .name(name)
                .user(owner)
                .address("address")
                .openTime(LocalTime.MIN)
                .closeTime(LocalTime.MAX)
                .depositAmount(1000000)
                .build();
        return storeRepository.save(store);
    }

    private Waiting createWaiting(Store store, User user, WaitingStatus status) {
        Waiting waiting = Waiting.builder()
                .store(store)
                .user(user)
                .orderId(UUID.randomUUID().toString())
                .peopleCount(2)
                .myWaitingOrder(0)
                .status(status)
                .build();
        return waitingRepository.save(waiting);
    }

    @AfterEach
    void tearDownBase() {
        String redisKey = WAITING_QUEUE_KEY_PREFIX + store.getId();
        waitingIdRedisTemplate.delete(redisKey);
    }

    @Test
    @DisplayName("사장: 요청->대기, 사용자: 요청->취소")
    void 요청_상태에서_사장님대기_사용자취소_중_하나만_성공한다() throws InterruptedException {

        for (int i = 0; i < repetitionCount; i++) {
            log.info("Iteration: {}", i + 1);

            // 각 반복마다 새로운 웨이팅 객체 생성
            Waiting waiting = createWaiting(store, user1, WaitingStatus.REQUESTED);
            Long waitingId = waiting.getId();
            Long ownerId = owner.getId();
            Long user1Id = user1.getId();

            ExecutorService executorService = Executors.newFixedThreadPool(2); // 2개의 스레드 풀 생성
            CountDownLatch latch = new CountDownLatch(2); // 2개의 작업 완료 대기
            AtomicBoolean waitingSuccess = new AtomicBoolean(false); // 사장님 대기 성공 여부
            AtomicBoolean cancelSuccess = new AtomicBoolean(false); // 사용자 취소 성공 여부
            AtomicReference<Throwable> exceptionHolder = new AtomicReference<>(); // 예외 발생 시 저장

            // 사장님 호출 스레드
            Runnable ownerCallTask = () -> {
                transactionTemplate.execute(status -> {
                    try {
                        Waiting lockedWaiting = waitingRepository.findByIdWithPessimisticLock(waitingId).orElseThrow();
                        Thread.sleep(new Random().nextInt(50) + 50); // 약간의 딜레이
                        if (lockedWaiting.getStatus() == WaitingStatus.REQUESTED) {
                            waitingService.updateWaitingStatus(ownerId, waitingId, UpdateWaitingRequest.from(WaitingStatus.WAITING));
                            waitingSuccess.set(true);
                        }
                    } catch (Throwable e) {
                        exceptionHolder.set(e);
                        status.setRollbackOnly();
                    }
                    return null;
                });
                latch.countDown();
            };

            // 사용자 취소 스레드
            Runnable userCancelTask = () -> {
                transactionTemplate.execute(status -> {
                    try {
                        Waiting lockedWaiting = waitingRepository.findByIdWithPessimisticLock(waitingId).orElseThrow();
                        if (lockedWaiting.getStatus() == WaitingStatus.REQUESTED) {
                            waitingService.cancelMyWaiting(user1Id, waitingId);
                            cancelSuccess.set(true);
                        }
                    } catch (Throwable e) {
                        exceptionHolder.set(e);
                        status.setRollbackOnly();
                    }
                    return null;
                });
                latch.countDown();
            };

            executorService.submit(ownerCallTask);
            executorService.submit(userCancelTask);

            latch.await();
            executorService.shutdownNow();

            // 예외 발생 여부 확인
            if (exceptionHolder.get() != null) {
                log.error("[{}] 예외 발생: {}", i + 1, exceptionHolder.get().getMessage());
                throw new AssertionError("[" + (i + 1) + "] 예외 발생: " + exceptionHolder.get().getMessage());
            }

            Waiting result = waitingRepository.findById(waitingId).orElseThrow();
            log.info("[{}] 최종 상태: {}", i + 1, result.getStatus());
            log.info("[{}] 사장님 대기 성공: {}", i + 1, waitingSuccess.get());
            log.info("[{}] 사용자 취소 성공: {}", i + 1, cancelSuccess.get());

            // 둘 중 하나만 성공하거나, 둘 다 실패하고 최종 상태가 WAITING이어야 함
            if (waitingSuccess.get() && cancelSuccess.get()) {
                fail("[" + (i + 1) + "] 사장님 호출과 사용자 취소가 모두 성공했습니다. (비관적 락 실패)");
            } else if (waitingSuccess.get()) {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.WAITING);
            } else if (cancelSuccess.get()) {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
            } else {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.REQUESTED);
            }

            waitingRepository.delete(result); // 각 반복 후 데이터 삭제
        }
        log.info("반복 테스트 완료!");
    }


    @Test
    @DisplayName("사장: 대기->호출, 사용자: 대기->취소")
    void 대기_상태에서_사장님호출_사용자취소_중_하나만_성공한다() throws InterruptedException {

        for (int i = 0; i < repetitionCount; i++) {
            log.info("Iteration: {}", i + 1);

            // 각 반복마다 새로운 웨이팅 객체 생성 및 초기 상태 설정
            Waiting waiting = createWaiting(store, user1, WaitingStatus.REQUESTED);
            Long waitingId = waiting.getId();
            Long ownerId = owner.getId();
            waitingService.updateWaitingStatus(ownerId, waitingId, UpdateWaitingRequest.from(WaitingStatus.WAITING));

            ExecutorService executorService = Executors.newFixedThreadPool(2); // 2개의 스레드를 사용하는 스레드 풀 생성
            CountDownLatch latch = new CountDownLatch(2); // 2개의 작업 완료를 기다리는 CountDownLatch 생성
            AtomicBoolean callSuccess = new AtomicBoolean(false); // 사장님 호출 성공 여부를 원자적으로 관리 (각 반복마다 초기화)
            AtomicBoolean cancelSuccess = new AtomicBoolean(false); // 유저 취소 성공 여부를 원자적으로 관리 (각 반복마다 초기화)
            AtomicReference<Throwable> exceptionHolder = new AtomicReference<>(); // 스레드 내에서 발생한 예외를 안전하게 참조 (각 반복마다 초기화)

            Runnable callTaskWithLock = () -> {
                transactionTemplate.execute(status -> {
                    try {
                        Waiting lockedWaiting = waitingRepository.findByIdWithPessimisticLock(waitingId).orElseThrow();
                        Thread.sleep(new Random().nextInt(50) + 50); // 약간의 랜덤 딜레이 추가하여 더 현실적인 동시성 상황 연출
                        if (lockedWaiting.getStatus() == WaitingStatus.WAITING) {
                            waitingService.updateWaitingStatus(ownerId, waitingId, UpdateWaitingRequest.from(WaitingStatus.CALLED));
                            callSuccess.set(true);
                        }
                    } catch (Throwable e) {
                        exceptionHolder.set(e);
                        status.setRollbackOnly();
                    }
                    return null;
                });
                latch.countDown();
            };

            Runnable cancelTaskWithLock = () -> {
                transactionTemplate.execute(status -> {
                    try {
                        Waiting lockedWaiting = waitingRepository.findByIdWithPessimisticLock(waitingId).orElseThrow();
                        if (lockedWaiting.getStatus() == WaitingStatus.WAITING) {
                            waitingService.cancelMyWaiting(user1.getId(), waitingId);
                            cancelSuccess.set(true);
                        }
                    } catch (Throwable e) {
                        exceptionHolder.set(e);
                        status.setRollbackOnly();
                    }
                    return null;
                });
                latch.countDown();
            };

            executorService.submit(callTaskWithLock);
            executorService.submit(cancelTaskWithLock);

            latch.await();
            executorService.shutdownNow();

            // 각 반복마다 예외 발생 여부 확인
            if (exceptionHolder.get() != null) {
                log.error("[{}] 예외 발생: {}", i + 1, exceptionHolder.get().getMessage());
                throw new AssertionError("[" + (i + 1) + "] 예외 발생: " + exceptionHolder.get().getMessage());
            }

            Waiting result = waitingRepository.findById(waitingId).orElseThrow();
            log.info("[{}] 최종 상태: {}", i + 1, result.getStatus());
            log.info("[{}] 사장님 호출 성공: {}", i + 1, callSuccess.get());
            log.info("[{}] 사용자 취소 성공: {}", i + 1, cancelSuccess.get());

            assertThat(callSuccess.get() ^ cancelSuccess.get()).isTrue();

            if (callSuccess.get()) {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.CALLED);
            } else if (cancelSuccess.get()) {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
            } else {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.WAITING);
            }
            waitingRepository.delete(result); // 각 반복 후 생성된 웨이팅 데이터 삭제
        }
        log.info("반복 테스트 완료!");
    }

    @Test
    @DisplayName("사장: 호출->취소, 사용자: 호출->취소")
    void 호출_상태에서_사장님취소_사용자취소_중_하나만_성공한다() throws InterruptedException {

        for (int i = 0; i < repetitionCount; i++) {
            log.info("Iteration: {}", i + 1);

            // 각 반복마다 새로운 웨이팅 객체 생성 및 초기 상태 설정
            Waiting waiting = createWaiting(store, user1, WaitingStatus.REQUESTED);
            Long waitingId = waiting.getId();
            Long ownerId = owner.getId();
            waitingService.updateWaitingStatus(ownerId, waitingId, UpdateWaitingRequest.from(WaitingStatus.WAITING));
            waitingService.updateWaitingStatus(ownerId, waitingId, UpdateWaitingRequest.from(WaitingStatus.CALLED));

            ExecutorService executorService = Executors.newFixedThreadPool(2); // 2개의 스레드를 사용하는 스레드 풀 생성
            CountDownLatch latch = new CountDownLatch(2); // 2개의 작업 완료를 기다리는 CountDownLatch 생성
            AtomicBoolean ownerCancelSuccess = new AtomicBoolean(false); // 사장님 취소 성공 여부를 원자적으로 관리 (각 반복마다 초기화)
            AtomicBoolean userCancelSuccess = new AtomicBoolean(false); // 유저 취소 성공 여부를 원자적으로 관리 (각 반복마다 초기화)
            AtomicReference<Throwable> exceptionHolder = new AtomicReference<>(); // 스레드 내에서 발생한 예외를 안전하게 참조 (각 반복마다 초기화)

            Runnable callTaskWithLock = () -> {
                transactionTemplate.execute(status -> {
                    try {
                        Waiting lockedWaiting = waitingRepository.findByIdWithPessimisticLock(waitingId).orElseThrow();
                        Thread.sleep(new Random().nextInt(50) + 50); // 약간의 랜덤 딜레이 추가하여 더 현실적인 동시성 상황 연출
                        if (lockedWaiting.getStatus() == WaitingStatus.CALLED) {
                            waitingService.updateWaitingStatus(ownerId, waitingId, UpdateWaitingRequest.from(WaitingStatus.CANCELLED));
                            ownerCancelSuccess.set(true);
                        }
                    } catch (Throwable e) {
                        exceptionHolder.set(e);
                        status.setRollbackOnly();
                    }
                    return null;
                });
                latch.countDown();
            };

            Runnable cancelTaskWithLock = () -> {
                transactionTemplate.execute(status -> {
                    try {
                        Waiting lockedWaiting = waitingRepository.findByIdWithPessimisticLock(waitingId).orElseThrow();
                        if (lockedWaiting.getStatus() == WaitingStatus.CALLED) {
                            waitingService.cancelMyWaiting(user1.getId(), waitingId);
                            userCancelSuccess.set(true);
                        }
                    } catch (Throwable e) {
                        exceptionHolder.set(e);
                        status.setRollbackOnly();
                    }
                    return null;
                });
                latch.countDown();
            };

            executorService.submit(callTaskWithLock);
            executorService.submit(cancelTaskWithLock);

            latch.await();
            executorService.shutdownNow();

            // 각 반복마다 예외 발생 여부 확인
            if (exceptionHolder.get() != null) {
                log.error("[{}] 예외 발생: {}", i + 1, exceptionHolder.get().getMessage());
                throw new AssertionError("[" + (i + 1) + "] 예외 발생: " + exceptionHolder.get().getMessage());
            }

            Waiting result = waitingRepository.findById(waitingId).orElseThrow();
            log.info("[{}] 최종 상태: {}", i + 1, result.getStatus());
            log.info("[{}] 사장님 취소 성공: {}", i + 1, ownerCancelSuccess.get());
            log.info("[{}] 사용자 취소 성공: {}", i + 1, userCancelSuccess.get());

            assertThat(ownerCancelSuccess.get() ^ userCancelSuccess.get()).isTrue();

            if (ownerCancelSuccess.get()) {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
            } else if (userCancelSuccess.get()) {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
            } else {
                assertThat(result.getStatus()).isEqualTo(WaitingStatus.CALLED);
            }
            waitingRepository.delete(result); // 각 반복 후 생성된 웨이팅 데이터 삭제
        }
        log.info("반복 테스트 완료!");
    }

}