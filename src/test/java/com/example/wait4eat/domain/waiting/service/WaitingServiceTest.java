package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import com.example.wait4eat.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

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
    @Mock
    private Store mockStore; // Mock Store 객체 사용

    private Waiting waiting;

    @BeforeEach
    void setUp() {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", 1L);

        waiting = Waiting.builder()
                .store(mockStore)
                .user(user)
                .status(WaitingStatus.REQUESTED)
                .build();
        ReflectionTestUtils.setField(waiting, "id", 100L);
        ReflectionTestUtils.setField(waiting, "createdAt", LocalDateTime.now());

        waitingService = new WaitingService(waitingRepository, storeRepository, userRepository, redisTemplate);

        // 불필요한 스텁빙 제거
        // when(mockStore.getId()).thenReturn(10L);
        when(mockStore.getWaitingTeamCount()).thenReturn(0);
    }

    @Test
    void updateWaiting_메서드로_REQUESTED에서_WAITING으로_변경을_성공한다() {
        // Given
        WaitingStatus newStatus = WaitingStatus.WAITING;
        List<Waiting> reorderedList = List.of(waiting);

        when(waitingRepository.findByStoreIdAndStatusOrderByCreatedAtAsc(10L, WaitingStatus.WAITING))
                .thenReturn(reorderedList);

        doAnswer(invocation -> {
            when(mockStore.getWaitingTeamCount()).thenReturn(mockStore.getWaitingTeamCount() + 1);
            return null;
        }).when(mockStore).incrementWaitingTeamCount();

        // When
        boolean updated = waitingService.updateWaiting(newStatus, waiting);

        // Then
        assertTrue(updated);
        assertEquals(newStatus, waiting.getStatus());
        assertEquals(1, mockStore.getWaitingTeamCount());
        verify(waitingRepository, times(1)).findByStoreIdAndStatusOrderByCreatedAtAsc(10L, WaitingStatus.WAITING);
        verify(waitingRepository, times(1)).saveAll(reorderedList);
    }

    @Test
    void updateWaiting_메서드로_WAITING에서_CALLED으로_변경을_성공한다() {
        // Given
        waiting.waiting(LocalDateTime.now());
        //when(mockStore.getWaitingTeamCount()).thenReturn(1); // waiting() 호출로 증가
        WaitingStatus newStatus = WaitingStatus.CALLED;

        // When
        boolean updated = waitingService.updateWaiting(newStatus, waiting);

        // Then
        assertTrue(updated);
        assertEquals(newStatus, waiting.getStatus());
        // assertEquals(1, mockStore.getWaitingTeamCount()); // CALLED 시 감소 로직 없음
    }

    @Test
    void updateWaiting_메서드로_WAITING에서_CANCELLED로_변경시_가게_웨이팅_팀_수_감소한다() {
        // Given
        waiting.waiting(LocalDateTime.now());
        when(mockStore.getWaitingTeamCount()).thenReturn(1); // waiting() 호출로 증가
        WaitingStatus newStatus = WaitingStatus.CANCELLED;

        doAnswer(invocation -> {
            when(mockStore.getWaitingTeamCount()).thenReturn(mockStore.getWaitingTeamCount() - 1);
            return null;
        }).when(mockStore).decrementWaitingTeamCount();

        // When
        boolean updated = waitingService.updateWaiting(newStatus, waiting);

        // Then
        assertTrue(updated);
        assertEquals(newStatus, waiting.getStatus());
        assertEquals(0, mockStore.getWaitingTeamCount());
    }
}