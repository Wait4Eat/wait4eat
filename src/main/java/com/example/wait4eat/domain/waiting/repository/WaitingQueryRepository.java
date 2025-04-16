package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.dto.response.MyPastWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.MyWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface WaitingQueryRepository {

    Page<WaitingResponse> findWaitingsByStoreId(Long storeId, WaitingStatus status, Pageable pageable);

    Optional<MyWaitingResponse> findMyWaiting(Long userId);

    Page<MyPastWaitingResponse> findMyPastWaitings(Long userId, Pageable pageable);

    // 특정 가게의 주어진 상태에 해당하는 웨이팅 수 조회
    int countByStoreIdAndStatus(Long storeId, WaitingStatus status);
}
