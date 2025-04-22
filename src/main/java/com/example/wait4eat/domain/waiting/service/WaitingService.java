package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.waiting.dto.request.CreateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.request.UpdateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.response.*;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface WaitingService {

    CreateWaitingResponse createWaiting(Long userId, Long storeId, CreateWaitingRequest request);

    Page<WaitingResponse> getWaitings(Long userId, Long storeId, WaitingStatus status, Pageable pageable);

    MyWaitingResponse getMyWaiting(Long userId);

    Page<MyPastWaitingResponse> getMyPastWaitings(Long userId, Pageable pageable);

    CancelWaitingResponse cancelMyWaiting(Long userId, Long waitingId);

    UpdateWaitingResponse updateWaitingStatus(Long userId, Long waitingId, UpdateWaitingRequest updateWaitingRequest);

    boolean updateWaiting(WaitingStatus newStatus, Waiting waiting);

    int getCurrentWaitingTeamCount(Long storeId);

    void cleanupWaitingQueues();

}
