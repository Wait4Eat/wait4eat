package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @InjectMocks
    private WaitingService waitingService;

    private Store store;
    private User owner;
    private User user;
    private Waiting waiting;

    @BeforeEach
    void setUp() {
        Long ownerId = 1L;
        owner = User.builder()
                .email("owner@example.com")
                .nickname("owner")
                .password("encodedOwnerPassword")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner, "id", ownerId);

        Long storeId = 10L;
        store = Store.builder()
                .name("Test Store")
                .user(owner)
                .build();
        ReflectionTestUtils.setField(store, "id", storeId);

        Long userId = 2L;
        user = User.builder()
                .email("user@example.com")
                .nickname("user")
                .role(UserRole.ROLE_USER)
                .password("encodedCustomerPassword")
                .build();
        ReflectionTestUtils.setField(user, "id", userId);

        Long waitingId = 100L;
        waiting = Waiting.builder()
                .store(store)
                .user(user)
                .orderId(UUID.randomUUID().toString())
                .peopleCount(2)
                .myWaitingOrder(0)
                .status(WaitingStatus.REQUESTED)
                .build();
        ReflectionTestUtils.setField(waiting, "id", waitingId);
    }

    @Nested
    class 웨이팅_상태_변경시 {
        @Test
        @WithMockAuthUser(userId = 1L, email = "owner@example.com", role = UserRole.ROLE_OWNER)
        void REQUESTED_에서_WAITING_으로_변경_성공() {
            // given
            when(waitingRepository.findById(waiting.getId())).thenReturn(Optional.of(waiting));
            when(waitingRepository.findByStoreIdAndStatusOrderByActivatedAtAsc(store.getId(), WaitingStatus.WAITING))
                    .thenReturn(List.of(waiting));
            when(waitingRepository.countByStoreIdAndStatus(store.getId(), WaitingStatus.WAITING)).thenReturn(1);

            // when
            boolean updated = waitingService.updateWaiting(WaitingStatus.WAITING, waiting);

            // then
            assertTrue(updated);
            assertEquals(WaitingStatus.WAITING, waiting.getStatus());
            assertNotNull(waiting.getActivatedAt());
            verify(waitingRepository, times(1)).saveAll(anyList()); // reorderWaitingQueue 호출 검증
        }

        @Test
        @WithMockAuthUser(userId = 1L, email = "owner@example.com", role = UserRole.ROLE_OWNER)
        void WAITING_에서_CALLED_로_변경_성공() {
            // given
            when(waitingRepository.findByStoreIdAndStatusOrderByActivatedAtAsc(store.getId(), WaitingStatus.WAITING))
                    .thenReturn(List.of()); // 웨이팅 목록이 비어있다고 가정
            when(waitingRepository.countByStoreIdAndStatus(store.getId(), WaitingStatus.WAITING)).thenReturn(0);
            waiting.waiting(LocalDateTime.now());

            // when
            boolean updated = waitingService.updateWaiting(WaitingStatus.CALLED, waiting);

            // then
            assertTrue(updated);
            assertEquals(WaitingStatus.CALLED, waiting.getStatus());
            assertNotNull(waiting.getCalledAt());
            assertEquals(0, waiting.getMyWaitingOrder()); // 호출되면 순서 0으로 변경 확인
            verify(waitingRepository, times(1)).saveAll(anyList()); // reorderWaitingQueue 호출 검증
        }

        @Test
        @WithMockAuthUser(userId = 1L, email = "user@example.com", role = UserRole.ROLE_USER) // 일반 유저는 취소만 가능
        void REQUESTED_에서_CANCELLED_로_변경_성공() {
            // given & when
            boolean updated = waitingService.updateWaiting(WaitingStatus.CANCELLED, waiting);

            // then
            assertTrue(updated);
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
            assertNotNull(waiting.getCancelledAt());
            verify(waitingRepository, never()).saveAll(anyList()); // reorderWaitingQueue 호출 안됨 확인
        }

        @Test
        @WithMockAuthUser(userId = 1L, email = "owner@example.com", role = UserRole.ROLE_OWNER)
        void WAITING_에서_CANCELLED_로_변경_성공() {
            // given
            waiting.waiting(LocalDateTime.now());
            when(waitingRepository.findByStoreIdAndStatusOrderByActivatedAtAsc(store.getId(), WaitingStatus.WAITING))
                    .thenReturn(List.of());
            when(waitingRepository.countByStoreIdAndStatus(store.getId(), WaitingStatus.WAITING)).thenReturn(0);

            // when
            boolean updated = waitingService.updateWaiting(WaitingStatus.CANCELLED, waiting);

            // then
            assertTrue(updated);
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
            assertNotNull(waiting.getCancelledAt());
            verify(waitingRepository, times(1)).saveAll(anyList()); // reorderWaitingQueue 호출 검증
        }

        @Test
        @WithMockAuthUser(userId = 1L, email = "owner@example.com", role = UserRole.ROLE_OWNER)
        void CALLED_에서_CANCELLED_로_변경_성공() {
            // given
            waiting.waiting(LocalDateTime.now());
            waiting.call(LocalDateTime.now());
            when(waitingRepository.findByStoreIdAndStatusOrderByActivatedAtAsc(store.getId(), WaitingStatus.WAITING))
                    .thenReturn(List.of());
            when(waitingRepository.countByStoreIdAndStatus(store.getId(), WaitingStatus.WAITING)).thenReturn(0);

            // when
            boolean updated = waitingService.updateWaiting(WaitingStatus.CANCELLED, waiting);

            // then
            assertTrue(updated);
            assertEquals(WaitingStatus.CANCELLED, waiting.getStatus());
            assertNotNull(waiting.getCancelledAt());
            verify(waitingRepository, times(1)).saveAll(anyList()); // reorderWaitingQueue 호출 검증
        }

        @Test
        @WithMockAuthUser(userId = 1L, email = "owner@example.com", role = UserRole.ROLE_OWNER)
        void CALLED_에서_COMPLETED_로_변경_성공() {
            // given
            when(waitingRepository.findByStoreIdAndStatusOrderByActivatedAtAsc(store.getId(), WaitingStatus.WAITING))
                    .thenReturn(List.of());
            when(waitingRepository.countByStoreIdAndStatus(store.getId(), WaitingStatus.WAITING)).thenReturn(0);
            waiting.waiting(LocalDateTime.now()); // 먼저 WAITING 상태로
            waiting.call(LocalDateTime.now());   // 그 다음 CALLED 상태로

            // when
            boolean updated = waitingService.updateWaiting(WaitingStatus.COMPLETED, waiting);

            // then
            assertTrue(updated);
            assertEquals(WaitingStatus.COMPLETED, waiting.getStatus());
            assertNotNull(waiting.getEnteredAt());
            assertEquals(0, waiting.getMyWaitingOrder()); // 완료되면 순서 0으로 유지 확인
            verify(waitingRepository, times(1)).saveAll(anyList()); // reorderWaitingQueue 호출 검증
        }

        @Test
        @WithMockAuthUser(userId = 1L, email = "owner@example.com", role = UserRole.ROLE_OWNER)
        void 유효하지_않은_상태_변경_시_예외_발생() {
            // given
            waiting.waiting(LocalDateTime.now());

            // when & then
            CustomException exception = assertThrows(CustomException.class,
                    () -> waitingService.updateWaiting(WaitingStatus.REQUESTED, waiting));

            assertEquals(ExceptionType.INVALID_WAITING_STATUS_UPDATE, exception.getExceptionType());
            assertEquals(String.format("현재 상태: %s, 요청 상태: %s", WaitingStatus.WAITING, WaitingStatus.REQUESTED),
                    exception.getMessage());
        }
    }
}