package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.waiting.dto.request.UpdateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.response.MyPastWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.MyWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.UpdateWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;

    @Transactional(readOnly = true)
    public Page<WaitingResponse> getWaitings(Long userId, Long storeId, WaitingStatus status, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        return waitingRepository.findWaitingsByStoreId(storeId, status, pageable);
    }

    @Transactional(readOnly = true)
    public MyWaitingResponse getMyWaiting(Long userId) {
        return waitingRepository.findMyWaiting(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.NO_CURRENT_WAITING));
    }

    @Transactional(readOnly = true)
    public Page<MyPastWaitingResponse> getMyPastWaitings(Long userId, Pageable pageable) {
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        return waitingRepository.findMyPastWaitings(userId, pageable);
    }

    @Transactional
    public void cancelMyWaiting(Long userId, Long waitingId) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

        if (!waiting.getUser().getId().equals(userId)) {
            throw new CustomException(ExceptionType.UNAUTHORIZED_CANCEL_WAITING);
        }

        if (waiting.getStatus() == WaitingStatus.CANCELLED || waiting.getStatus() == WaitingStatus.COMPLETED) {
            throw new CustomException(ExceptionType.ALREADY_FINISHED_WAITING);
        }

        updateWaitingToCancelled(waiting, getCurrentTime());
    }

    @Transactional
    public UpdateWaitingResponse updateWaitingStatus(Long userId, Long waitingId, UpdateWaitingRequest updateWaitingRequest) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

        WaitingStatus newStatus = updateWaitingRequest.getStatus();
        WaitingStatus currentStatus = waiting.getStatus();

        waiting.updateStatus(newStatus);

        LocalDateTime now = getCurrentTime();

        if (newStatus == WaitingStatus.CALLED && currentStatus != WaitingStatus.CALLED && waiting.getCalledAt() == null) {
            updateWaitingToCalled(waiting, now);
        } else if (newStatus == WaitingStatus.CANCELLED && currentStatus != WaitingStatus.CANCELLED && waiting.getCancelledAt() == null) {
            updateWaitingToCancelled(waiting, now);
        } else if (newStatus == WaitingStatus.COMPLETED && currentStatus != WaitingStatus.COMPLETED && waiting.getEnteredAt() == null) {
            updateWaitingToCompleted(waiting, now);
        }

        return UpdateWaitingResponse.builder()
                .waitingId(waiting.getId())
                .status(waiting.getStatus())
                .calledAt(waiting.getCalledAt())
                .cancelledAt(waiting.getCancelledAt())
                .enteredAt(waiting.getEnteredAt())
                .build();
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }

    private void updateWaitingToCalled(Waiting waiting, LocalDateTime now) {
        if (waiting.getStatus() != WaitingStatus.CALLED && waiting.getCalledAt() == null) {
            waiting.call(now);
        }
    }

    private void updateWaitingToCancelled(Waiting waiting, LocalDateTime now) {
        if (waiting.getStatus() != WaitingStatus.CANCELLED && waiting.getCancelledAt() == null) {
            waiting.cancel(now);
        }
    }

    private void updateWaitingToCompleted(Waiting waiting, LocalDateTime now) {
        if (waiting.getStatus() != WaitingStatus.COMPLETED && waiting.getEnteredAt() == null) {
            waiting.enter(now);
        }
    }
}
