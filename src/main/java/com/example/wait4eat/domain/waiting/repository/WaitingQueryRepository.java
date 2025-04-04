package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WaitingQueryRepository {
    Page<WaitingResponse> findWaitingsByStoreId(Long storeId, Pageable pageable);
}
