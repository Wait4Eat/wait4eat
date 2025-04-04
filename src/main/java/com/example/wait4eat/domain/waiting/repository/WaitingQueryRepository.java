package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WaitingQueryRepository {
    Page<WaitingResponse> findWaitingsByStoreId(Long storeId, WaitingStatus status, Pageable pageable);
}
