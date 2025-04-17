package com.example.wait4eat.domain.waiting.entity;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class WaitingTest {

    @Test
    void requested_상태에서_waiting_메서드를_호출하면_상태가_waiting으로_변경되고_활성화시간이_설정된다() {
        // given
        // Store 객체의 Mock 생성 및 getId() 메서드가 1L을 반환하도록 설정
        Store mockStore = mock(Store.class);
        when(mockStore.getId()).thenReturn(1L);

        // User 객체의 Mock 생성 및 getId() 메서드가 1L을 반환하도록 설정
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);

        // Waiting 객체 생성 (초기 상태: REQUESTED)
        Waiting waiting = Waiting.builder()
                .store(mockStore)
                .user(mockUser)
                .orderId(UUID.randomUUID().toString())
                .peopleCount(2)
                .myWaitingOrder(0)
                .status(WaitingStatus.REQUESTED)
                .build();
        // 웨이팅 상태 변경 시 설정될 활성화 시간
        LocalDateTime now = LocalDateTime.now();

        // when
        // Waiting 객체의 waiting() 메서드 호출 (상태 변경 액션)
        waiting.waiting(now);

        // then
        // 상태가 WAITING으로 변경되었는지 검증
        assertEquals(WaitingStatus.WAITING, waiting.getStatus());
        // 활성화 시간이 null이 아닌지 검증 (waiting() 메서드에서 설정되었는지 확인)
        assertNotNull(waiting.getActivatedAt());
    }
}