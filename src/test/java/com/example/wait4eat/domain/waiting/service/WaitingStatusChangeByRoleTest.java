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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class IndividualUserOwnerWaitingStatusChangeTest {

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


        // currentWaitings 초기화 및 mock 설정
        currentWaitings.clear();
    }

    private User createUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Waiting createWaiting(Long id, User user, Store store, int peopleCount, WaitingStatus status) {
        Waiting waiting = Waiting.builder()
                .user(user)
                .store(store)
                .peopleCount(peopleCount)
                .status(status)
                .build();
        ReflectionTestUtils.setField(waiting, "id", id);
        return waiting;
    }

    private void printCurrentWaitingQueue() {
        List<Waiting> waitingQueue = currentWaitings.stream()
                .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                .sorted(Comparator.comparing(Waiting::getMyWaitingOrder))
                .collect(Collectors.toList());
        log.info("------ 현재 대기열 상태 ------");
        if (waitingQueue.isEmpty()) {
            log.info("  비어있음");
        } else {
            waitingQueue.forEach(w -> log.info("  ID: {}, 순서: {}, 상태: {}, 생성시간: {}", w.getId(), w.getMyWaitingOrder(), w.getStatus(), w.getActivatedAt()));
        }
        log.info("--------------------------");
    }

    @Test
    @DisplayName("대기열 추가 및 확인")
    void createAndCheckWaitingQueue() {
        // Given
        User user1 = createUser(1L);
        CreateWaitingRequest createRequest1 = CreateWaitingRequest.builder().peopleCount(2).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        User user2 = createUser(2L);
        CreateWaitingRequest createRequest2 = CreateWaitingRequest.builder().peopleCount(3).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
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

        // When
        CreateWaitingResponse response1 = waitingService.createWaiting(1L, storeId, createRequest1);
        CreateWaitingResponse response2 = waitingService.createWaiting(2L, storeId, createRequest2);

        List<Waiting> waitingQueue = currentWaitings.stream()
                .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                .toList();

        // Then
        assertEquals(1L, response1.getWaitingId());
        assertEquals(2L, response2.getWaitingId());
        // 웨이팅 생성 직후 상태는 REQUESTED 이므로 대기열(WAITING) 크기는 0이어야 합니다.
        assertEquals(0, waitingQueue.size());
        assertTrue(currentWaitings.stream().anyMatch(w -> w.getId() == 1L && w.getUser().getId() == 1L && w.getPeopleCount() == 2 && w.getStatus() == WaitingStatus.REQUESTED));
        assertTrue(currentWaitings.stream().anyMatch(w -> w.getId() == 2L && w.getUser().getId() == 2L && w.getPeopleCount() == 3 && w.getStatus() == WaitingStatus.REQUESTED));
    }

    @Nested
    @DisplayName("사용자 웨이팅 취소")
    class UserCancelWaitingTest {

        @Test
        @DisplayName("REQUESTED 상태에서 취소")
        void cancelRequestedWaiting() {
            // Given
            User user = createUser(1L);
            Waiting waiting = createWaiting(1L, user, store, 2, WaitingStatus.REQUESTED);
            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
            printCurrentWaitingQueue();

            // When
            waitingService.cancelMyWaiting(1L, 1L);

            // Then
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
            printCurrentWaitingQueue();
        }

        @Test
        @DisplayName("WAITING 상태에서 취소")
        void cancelWaitingInQueue() {
            // Given
            User user = createUser(1L);
            Waiting waiting = createWaiting(1L, user, store, 2, WaitingStatus.WAITING);
            currentWaitings.add(waiting);
            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
            printCurrentWaitingQueue();

            // When
            waitingService.cancelMyWaiting(1L, 1L);
            printCurrentWaitingQueue();

            // Then
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
        }

        @Test
        @DisplayName("CALLED 상태에서 취소")
        void cancelCalledWaiting() {
            // Given
            User user = createUser(1L);
            Waiting waiting = createWaiting(1L, user, store, 2, WaitingStatus.CALLED);
            currentWaitings.add(waiting);
            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
            printCurrentWaitingQueue();

            // When
            waitingService.cancelMyWaiting(1L, 1L);
            printCurrentWaitingQueue();

            // Then
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
        }
    }

    @Nested
    @DisplayName("사장님이 웨이팅 상태 변경")
    class OwnerWaitingTest {
        @Test
        @DisplayName("REQUESTED에서 CANCELLED으로 변경")
        void changeRequestedToCancelled() {
            // Given
            User user = createUser(1L);
            Waiting waiting = createWaiting(1L, user, store, 2, WaitingStatus.REQUESTED);
            currentWaitings.add(waiting);
            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
            printCurrentWaitingQueue();

            // When
            waitingService.updateWaitingStatus(owner.getId(), 1L, UpdateWaitingRequest.builder().status(WaitingStatus.CANCELLED).build());
            printCurrentWaitingQueue();

            // Then
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
        }

        @Test
        @DisplayName("WAITING에서 CANCELLED으로 변경")
        void changeWaitingToCancelled() {
            // Given
            User user = createUser(1L);
            Waiting waiting = createWaiting(1L, user, store, 2, WaitingStatus.WAITING);
            currentWaitings.add(waiting);
            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
            printCurrentWaitingQueue();

            List<Waiting> waitingQueueBefore = currentWaitings.stream()
                    .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                    .toList();
            int initialWaitingQueueSize = waitingQueueBefore.size();

            // When
            waitingService.updateWaitingStatus(owner.getId(), 1L, UpdateWaitingRequest.builder().status(WaitingStatus.CANCELLED).build());
            printCurrentWaitingQueue();

            List<Waiting> waitingQueueAfter = currentWaitings.stream()
                    .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                    .toList();

            // Then
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
            assertEquals(initialWaitingQueueSize - 1, waitingQueueAfter.size());
        }

        @Test
        @DisplayName("CALLED에서 CANCELLED으로 변경")
        void changeCalledToCancelled() {
            // Given
            User user = createUser(1L);
            Waiting waiting = createWaiting(1L, user, store, 2, WaitingStatus.CALLED);
            currentWaitings.add(waiting);
            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
            printCurrentWaitingQueue();

            // When
            waitingService.updateWaitingStatus(owner.getId(), 1L, UpdateWaitingRequest.builder().status(WaitingStatus.CANCELLED).build());
            printCurrentWaitingQueue();

            // Then
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
            List<Waiting> waitingQueueAfter = currentWaitings.stream()
                    .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                    .collect(Collectors.toList());
            assertEquals(0, waitingQueueAfter.size()); // CALLED 상태는 대기열에 없으므로 변화 없음
        }

        @Test
        @DisplayName("REQUESTED에서 WAITING으로 변경")
        void changeRequestedToWaiting() {
            // Given
            User user1 = createUser(1L);
            Waiting waiting1 = createWaiting(1L, user1, store, 2, WaitingStatus.REQUESTED);
            currentWaitings.add(waiting1);
            printCurrentWaitingQueue();

            User user2 = createUser(2L);
            Waiting waiting2 = createWaiting(2L, user2, store, 3, WaitingStatus.REQUESTED);
            currentWaitings.add(waiting2);

            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting1));
            when(waitingRepository.findById(2L)).thenReturn(Optional.of(waiting2));
            when(waitingRepository.findByStoreIdAndStatusOrderByActivatedAtAsc(eq(storeId), eq(WaitingStatus.WAITING)))
                    .thenAnswer(invocation -> currentWaitings.stream()
                            .filter(w -> w.getStatus() == WaitingStatus.WAITING)
                            .sorted(Comparator.comparing(Waiting::getActivatedAt))
                            .collect(Collectors.toList()));

            // When
            waitingService.updateWaitingStatus(owner.getId(), 1L, UpdateWaitingRequest.builder().status(WaitingStatus.WAITING).build());
            waitingService.updateWaitingStatus(owner.getId(), 2L, UpdateWaitingRequest.builder().status(WaitingStatus.WAITING).build());

            List<Waiting> waitingQueue = waitingRepository.findByStoreIdAndStatusOrderByActivatedAtAsc(storeId, WaitingStatus.WAITING);
            printCurrentWaitingQueue(); // 재활용된 메서드 호출

            // Then
            assertEquals(2, waitingQueue.size());
            assertEquals(WaitingStatus.WAITING, waitingQueue.get(0).getStatus());
            assertEquals(WaitingStatus.WAITING, waitingQueue.get(1).getStatus());
            assertEquals(1, waitingQueue.get(0).getMyWaitingOrder());
            assertEquals(2, waitingQueue.get(1).getMyWaitingOrder());
        }

        @Test
        @DisplayName("WAITING에서 CALLED로 변경")
        void changeWaitingToCalled() {
            // Given
            User user = createUser(1L);
            Waiting waiting = createWaiting(1L, user, store, 2, WaitingStatus.WAITING);
            waiting.myWaitingOrder(1);
            currentWaitings.add(waiting);
            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
            printCurrentWaitingQueue(); // 재활용된 메서드 호출

            // When
            waitingService.updateWaitingStatus(owner.getId(), 1L, UpdateWaitingRequest.builder().status(WaitingStatus.CALLED).build());
            printCurrentWaitingQueue(); // 재활용된 메서드 호출

            // Then
            assertEquals(WaitingStatus.CALLED, waiting.getStatus());
        }

        @Test
        @DisplayName("CALLED에서 COMPLETED로 변경")
        void changeCalledToCompleted() {
            // Given
            User user = createUser(1L);
            Waiting waiting = createWaiting(1L, user, store, 2, WaitingStatus.CALLED);
            currentWaitings.add(waiting);
            when(waitingRepository.findById(1L)).thenReturn(Optional.of(waiting));
            printCurrentWaitingQueue(); // 재활용된 메서드 호출

            // When
            waitingService.updateWaitingStatus(owner.getId(), 1L, UpdateWaitingRequest.builder().status(WaitingStatus.COMPLETED).build());
            printCurrentWaitingQueue(); // 재활용된 메서드 호출

            // Then
            assertEquals(WaitingStatus.COMPLETED, waiting.getStatus());
        }

    }
}