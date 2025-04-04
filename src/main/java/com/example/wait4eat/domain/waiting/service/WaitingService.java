package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;

    public Page<WaitingResponse> getWaitings(Long storeId, Pageable pageable) {
        return null;
        //return waitingRepository.findWaitingsByStoreId(storeId, pageable);
    }
}
