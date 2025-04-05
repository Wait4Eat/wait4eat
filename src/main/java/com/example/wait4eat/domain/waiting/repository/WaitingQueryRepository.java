package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.dto.response.MyWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface WaitingQueryRepository {
    Page<WaitingResponse> findWaitingsByStoreId(Long storeId, WaitingStatus status, Pageable pageable);
    Optional<MyWaitingResponse> findMyWaiting(Long userId);
}
