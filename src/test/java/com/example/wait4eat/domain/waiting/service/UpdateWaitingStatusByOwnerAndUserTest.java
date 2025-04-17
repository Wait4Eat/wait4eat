package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.dto.request.CreateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.request.UpdateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.response.CreateWaitingResponse;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UpdateWaitingStatusByOwnerAndUserTest {

    @InjectMocks
    private WaitingService waitingService;

    @Mock
    private WaitingRepository waitingRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private Long storeId = 1L;
    private User owner;
    private Store store;
    private List<Waiting> currentWaitings = new ArrayList<>();
    private AtomicLong userIdCounter = new AtomicLong(1);
    private AtomicLong waitingIdCounter = new AtomicLong(1);
    private final List<Long> createdWaitingIds = new ArrayList<>();
    private final Map<Long, Long> userIdToWaitingIdMap = new ConcurrentHashMap<>(); // 사용자 ID와 웨이팅 ID 매핑

    @BeforeEach
    void setUp() {
        // 스토어 생성
        owner = User.builder()
                .email("owner@example.com")
                .nickname("owner")
                .password("encodedOwnerPassword")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner, "id", 100L);

        store = Store.builder()
                .name("Test Store")
                .user(owner)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // currentWaitings 초기화 및 mock 설정
        currentWaitings.clear();
        createdWaitingIds.clear();
        userIdToWaitingIdMap.clear();

        when(waitingRepository.findByStoreIdAndStatusOrderByActivatedAtAsc(eq(storeId), eq(WaitingStatus.WAITING)))
                .thenAnswer(invocation -> currentWaitings.stream()
                        .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                        .sorted(Comparator.comparing(Waiting::getActivatedAt))
                        .collect(Collectors.toList()));

        when(waitingRepository.findByUserIdAndStatusIn(anyLong(), anyList()))
                .thenAnswer(invocation -> currentWaitings.stream()
                        .filter(w -> w.getUser().getId().equals(invocation.getArgument(0)) &&
                                ((List<WaitingStatus>) invocation.getArgument(1)).contains(w.getStatus()))
                        .findFirst());

        when(waitingRepository.findById(anyLong())).thenAnswer(invocation ->
                currentWaitings.stream().filter(w -> w.getId().equals(invocation.getArgument(0))).findFirst());

        when(waitingRepository.save(any(Waiting.class))).thenAnswer(invocation -> {
            Waiting waiting = invocation.getArgument(0);
            if (waiting.getId() == null) {
                ReflectionTestUtils.setField(waiting, "id", waitingIdCounter.getAndIncrement());
            }
            ReflectionTestUtils.setField(waiting, "store", store); // Waiting 객체에 store 설정
            currentWaitings.removeIf(w -> w.getId() != null && w.getId().equals(waiting.getId()));
            currentWaitings.add(waiting);
            return waiting;
        });

        when(waitingRepository.saveAll(anyCollection())).thenAnswer(invocation -> {
            Collection<Waiting> waitingsToSave = invocation.getArgument(0);
            waitingsToSave.forEach(waiting -> {
                ReflectionTestUtils.setField(waiting, "store", store); // Waiting 객체에 store 설정
                currentWaitings.removeIf(w -> w.getId() != null && w.getId().equals(waiting.getId()));
                currentWaitings.add(waiting);
            });
            return new ArrayList<>(waitingsToSave);
        });

        when(waitingRepository.countByStoreIdAndStatus(eq(storeId), eq(WaitingStatus.WAITING)))
                .thenAnswer(invocation -> (int) currentWaitings.stream().filter(w -> w.getStatus() == WaitingStatus.WAITING).count());

        when(userRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            User user = User.builder().build();
            ReflectionTestUtils.setField(user, "id", id); // ReflectionTestUtils로 id 설정
            return Optional.of(user);
        });
    }

    private void printCurrentWaitingQueue() {
        List<Waiting> waitingQueue = currentWaitings.stream()
                .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                .sorted(Comparator.comparing(Waiting::getMyWaitingOrder))
                .collect(Collectors.toList());
        log.info("--- 현재 대기열 상태 ---");
        if (waitingQueue.isEmpty()) {
            log.info("  비어있음");
        } else {
            waitingQueue.forEach(w -> log.info("  ID: {}, 순서: {}, 상태: {}, 생성시간: {}", w.getId(), w.getMyWaitingOrder(), w.getStatus(), w.getActivatedAt()));
        }
        log.info("--------------------------");
    }

    @Test
    void testOwnerAndUserCancelWaitingByStatus() throws InterruptedException {
        final int numberOfUsers = 50;
        final int ownerActionIntervalMillis = 5000;
        final int numberOfWaitingsToActivate = 10;
        final int numberOfUsersToCancel = 10;
        final int userCancelDelayMillis = 2500;

        // 사용자 웨이팅 추가
        Thread userCreationThread = new Thread(() -> {
            for (int i = 1; i <= numberOfUsers; i++) {
                long currentUserId = userIdCounter.getAndIncrement();
                CreateWaitingRequest createRequest = CreateWaitingRequest.builder()
                        .peopleCount(2)
                        .build();
                CreateWaitingResponse createdWaitingResponse = waitingService.createWaiting(currentUserId, storeId, createRequest);
                createdWaitingIds.add(createdWaitingResponse.getWaitingId());
                userIdToWaitingIdMap.put(currentUserId, createdWaitingResponse.getWaitingId());
                log.info("사용자 {} 웨이팅 생성 요청 (ID: {})", currentUserId, createdWaitingResponse.getWaitingId());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        userCreationThread.start();
        userCreationThread.join(numberOfUsers * 100 + 2000);

        log.info("--- 초기 웨이팅 (REQUESTED 상태, 총 {} 건) ---", currentWaitings.size());

        // 사용자 취소 액션 스레드
        Thread userCancelThread = new Thread(() -> {
            try {
                Thread.sleep(userCancelDelayMillis); // 웨이팅 생성 및 일부 사장 액션 후 취소 시작
                int cancelledCount = 0;
                for (Long userId : userIdToWaitingIdMap.keySet()) {
                    if (cancelledCount >= numberOfUsersToCancel) {
                        break;
                    }
                    Long waitingIdToCancel = userIdToWaitingIdMap.get(userId);
                    Waiting waitingToCancel = currentWaitings.stream().filter(w -> w.getId().equals(waitingIdToCancel)).findFirst().orElse(null);
                    if (waitingToCancel != null) {
                        WaitingStatus currentStatus = waitingToCancel.getStatus();
                        int lastDigit = (int) (waitingIdToCancel % 10);

                        if ((lastDigit == 5 && currentStatus == WaitingStatus.REQUESTED) ||
                                (lastDigit == 6 && currentStatus == WaitingStatus.WAITING) ||
                                (lastDigit == 7 && currentStatus == WaitingStatus.CALLED)) {
                            log.info("--- 사용자 {} 웨이팅 취소 요청 (ID: {}, 상태: {}) ---", userId, waitingToCancel.getId(), currentStatus);
                            waitingService.cancelMyWaiting(userId, waitingToCancel.getId()); // 메서드 이름 수정
                            printCurrentWaitingQueue();
                            cancelledCount++;
                        }
                    }
                    Thread.sleep(300); // 취소 요청 간 텀
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        userCancelThread.start();

        // 사장의 액션 스레드: REQUESTED -> WAITING 및 특정 상태 변경
        Thread ownerActionThread = new Thread(() -> {
            try {
                Thread.sleep(3000); // 초기 웨이팅 생성을 기다림
                int activationCycle = 0;
                while (activationCycle * numberOfWaitingsToActivate < numberOfUsers) {
                    // REQUESTED -> CANCELLED (사장)
                    log.info("--- 사장 액션 (Cycle {}): REQUESTED -> CANCELLED 처리 ---", activationCycle + 1);
                    for (Long waitingId : createdWaitingIds) {
                        Waiting waiting = currentWaitings.stream().filter(w -> w.getId().equals(waitingId)).findFirst().orElse(null);
                        if (waiting != null && waiting.getStatus() == WaitingStatus.REQUESTED && (waitingId % 10 == 4)) {
                            log.info("--- 사장 (Cycle {}) 웨이팅 ID {} 상태 변경 (REQUESTED -> CANCELLED) ---", activationCycle + 1, waitingId);
                            waitingService.updateWaitingStatus(owner.getId(), waitingId, UpdateWaitingRequest.builder().status(WaitingStatus.CANCELLED).build());
                            printCurrentWaitingQueue();
                        }
                        Thread.sleep(50);
                    }

                    // REQUESTED -> WAITING (사장)
                    List<Waiting> requestedWaitings = currentWaitings.stream()
                            .filter(w -> w.getStatus() == WaitingStatus.REQUESTED && (w.getId() % 10 != 4))
                            .limit(numberOfWaitingsToActivate)
                            .collect(Collectors.toList());

                    if (!requestedWaitings.isEmpty()) {
                        log.info("--- 사장 액션 (Cycle {}): {}개의 웨이팅 상태 변경 (REQUESTED -> WAITING) ---", activationCycle + 1, requestedWaitings.size());
                        for (Waiting waiting : requestedWaitings) {
                            waitingService.updateWaitingStatus(owner.getId(), waiting.getId(), UpdateWaitingRequest.builder().status(WaitingStatus.WAITING).build());
                        }
                        printCurrentWaitingQueue();
                    }

                    // 나머지 상태 변경 (사장)
                    log.info("--- 사장 액션 (Cycle {}): 특정 웨이팅 상태 변경 ---", activationCycle + 1);
                    for (Long waitingId : createdWaitingIds) {
                        Waiting waiting = currentWaitings.stream().filter(w -> w.getId().equals(waitingId)).findFirst().orElse(null);
                        if (waiting != null) {
                            int lastDigit = (int) (waitingId % 10);

                            if (lastDigit == 1 && waiting.getStatus() == WaitingStatus.WAITING) {
                                log.info("--- 사장 (Cycle {}) 웨이팅 ID {} 상태 변경 (WAITING -> CALLED) ---", activationCycle + 1, waitingId);
                                waitingService.updateWaitingStatus(owner.getId(), waitingId, UpdateWaitingRequest.builder().status(WaitingStatus.CALLED).build());
                                printCurrentWaitingQueue();
                                log.info("--- 사장 (Cycle {}) 웨이팅 ID {} 상태 변경 (CALLED -> COMPLETED) ---", activationCycle + 1, waitingId);
                                waitingService.updateWaitingStatus(owner.getId(), waitingId, UpdateWaitingRequest.builder().status(WaitingStatus.COMPLETED).build());
                                printCurrentWaitingQueue();
                            } else if (lastDigit == 2) {
                                if (waiting.getStatus() == WaitingStatus.WAITING) {
                                    log.info("--- 사장 (Cycle {}) 웨이팅 ID {} 상태 변경 (WAITING -> CALLED) ---", activationCycle + 1, waitingId);
                                    waitingService.updateWaitingStatus(owner.getId(), waitingId, UpdateWaitingRequest.builder().status(WaitingStatus.CALLED).build());
                                    printCurrentWaitingQueue();
                                }
                                if (waiting.getStatus() == WaitingStatus.CALLED) {
                                    log.info("--- 사장 (Cycle {}) 웨이팅 ID {} 상태 변경 (CALLED -> CANCELLED) ---", activationCycle + 1, waitingId);
                                    waitingService.updateWaitingStatus(owner.getId(), waitingId, UpdateWaitingRequest.builder().status(WaitingStatus.CANCELLED).build());
                                    printCurrentWaitingQueue();
                                }
                            } else if (lastDigit == 3) {
                                if (waiting.getStatus() == WaitingStatus.WAITING) {
                                    log.info("--- 사장 (Cycle {}) 웨이팅 ID {} 상태 변경 (WAITING -> CANCELLED) ---", activationCycle + 1, waitingId);
                                    waitingService.updateWaitingStatus(owner.getId(), waitingId, UpdateWaitingRequest.builder().status(WaitingStatus.CANCELLED).build());
                                    printCurrentWaitingQueue();
                                }
                            }
                        }
                        Thread.sleep(50);
                    }

                    Thread.sleep(ownerActionIntervalMillis);
                    activationCycle++;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        ownerActionThread.start();

        // 모든 스레드 종료 대기
        userCancelThread.join();
        ownerActionThread.join(numberOfUsers / numberOfWaitingsToActivate * ownerActionIntervalMillis + 5000);

        // 최종 대기열 상태 출력
        log.info("--- 최종 대기열 상태 ---");
        printCurrentWaitingQueue();

        // 최종 상태별 웨이팅 수 출력
        log.info("--- 최종 상태별 웨이팅 수 ---");
        Map<WaitingStatus, Long> finalStatusCounts = currentWaitings.stream()
                .collect(Collectors.groupingBy(Waiting::getStatus, Collectors.counting()));
        finalStatusCounts.forEach((status, count) -> log.info("{}: {} 건", status, count));
        log.info("--------------------------");

        // 최종 대기열 검증 (남아있는 WAITING 상태의 순서 확인)
        List<Waiting> finalWaitingQueue = currentWaitings.stream()
                .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                .sorted(Comparator.comparing(Waiting::getMyWaitingOrder))
                .collect(Collectors.toList());

        for (int i = 0; i < finalWaitingQueue.size(); i++) {
            if (!finalWaitingQueue.isEmpty()) {
                assertEquals(i + 1, finalWaitingQueue.get(i).getMyWaitingOrder(), "대기열 순서가 올바르지 않습니다.");
            }
        }
    }
}