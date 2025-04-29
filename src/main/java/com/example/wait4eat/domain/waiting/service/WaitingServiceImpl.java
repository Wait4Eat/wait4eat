package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.dto.request.CreateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.request.UpdateWaitingRequest;
import com.example.wait4eat.domain.waiting.dto.response.*;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.event.WaitingCalledEvent;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingServiceImpl implements WaitingService {

    private final WaitingRepository waitingRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final WaitingRedisService waitingRedisService;


    // 웨이팅 상태 순서 정의: 상태 업데이트 시 순서 검증에 사용
    private static final List<WaitingStatus> statusOrder = List.of(
            WaitingStatus.REQUESTED,
            WaitingStatus.WAITING,
            WaitingStatus.CALLED,
            WaitingStatus.CANCELLED,
            WaitingStatus.COMPLETED
    );

    @Override
    @Retryable(
            retryFor = {LockTimeoutException.class, PessimisticLockException.class, DataAccessException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 5000, multiplier = 2)
    )
    @Transactional
    public CreateWaitingResponse createWaiting(Long userId, Long storeId, CreateWaitingRequest request) {
        try {
            // 유저에 비관적 락 걸기
            User user = userRepository.findByIdForUpdate(userId)
                    .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));

            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));

            // 활성 웨이팅 있는지 조회 (락 없음)
            boolean hasActiveWaiting = waitingRepository.existsByUserIdAndStatusIn(
                    userId,
                    List.of(WaitingStatus.REQUESTED, WaitingStatus.WAITING, WaitingStatus.CALLED)
            );

            if (hasActiveWaiting) {
                throw new CustomException(ExceptionType.SINGLE_WAIT_ALLOWED);
            }

            // 고유한 주문 ID 생성
            String orderId = UUID.randomUUID().toString();

            // 새로운 웨이팅 생성 및 저장
            Waiting waiting = Waiting.builder()
                    .store(store)
                    .user(user)
                    .orderId(orderId)
                    .peopleCount(request.getPeopleCount())
                    .myWaitingOrder(0) // 초기값 설정 (결제 후 업데이트)
                    .status(WaitingStatus.REQUESTED)
                    .build();

            Waiting savedWaiting = waitingRepository.save(waiting);

            return CreateWaitingResponse.from(savedWaiting);

        } catch (Exception e) {
            log.error("createWaiting 중 예외 발생: {}", e.getMessage());
            handleLockingExceptions(e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WaitingResponse> getWaitings(Long userId, Long storeId, WaitingStatus status, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));

        // 해당 userId가 가게를 생성한 본인인지 확인 (접근 권한 검사)
        if (!store.getUser().getId().equals(userId)) {
            throw new CustomException(ExceptionType.NO_PERMISSION_TO_ACCESS_STORE_WAITING);
        }

        return waitingRepository.findWaitingsByStoreId(storeId, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public MyWaitingResponse getMyWaiting(Long userId) {
        return waitingRepository.findMyWaiting(userId)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MyPastWaitingResponse> getMyPastWaitings(Long userId, Pageable pageable) {
        return waitingRepository.findMyPastWaitings(userId, pageable);
    }

    // 사용자의 웨이팅 취소
    @Override
    @Transactional
    public CancelWaitingResponse cancelMyWaiting(Long userId, Long waitingId) {
        try {

            // 비관적 락으로 웨이팅 정보 가져오기
            Waiting waiting = waitingRepository.findByIdForUpdate(waitingId)
                    .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

            // 취소 권한 확인 (본인인지 확인)
            if (!waiting.getUser().getId().equals(userId)) {
                throw new CustomException(ExceptionType.UNAUTHORIZED_CANCEL_WAITING);
            }

            // 이미 취소 또는 완료된 웨이팅인지 확인
            if (waiting.getStatus() == WaitingStatus.CANCELLED || waiting.getStatus() == WaitingStatus.COMPLETED) {
                throw new CustomException(ExceptionType.ALREADY_FINISHED_WAITING);
            }

            // 웨이팅 상태를 CANCELLED로 업데이트
            updateWaiting(WaitingStatus.CANCELLED, waiting);

            return CancelWaitingResponse.from(waiting);

        } catch (Exception e) {
            log.error("cancelMyWaiting 중 예외 발생: {}", e.getMessage());
            handleLockingExceptions(e);
            throw e;
        }
    }

    @Override
    @Transactional
    public UpdateWaitingResponse updateWaitingStatus(Long userId, Long waitingId, UpdateWaitingRequest updateWaitingRequest) {
        try {
            // 비관적 락으로 웨이팅 정보 가져오기
            Waiting waiting = waitingRepository.findByIdForUpdate(waitingId)
                    .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

            // 가게 주인 확인 로직
            if (!waiting.getStore().getUser().getId().equals(userId)) {
                throw new CustomException(ExceptionType.NO_PERMISSION_ACTION);
            }

            // 웨이팅 상태 업데이트
            updateWaiting(updateWaitingRequest.getStatus(), waiting);

            return UpdateWaitingResponse.from(waiting);

        } catch (Exception e) {
            log.error("updateWaitingStatus 중 예외 발생: {}", e.getMessage());
            handleLockingExceptions(e);
            throw e;
        }
    }

    private boolean updateWaiting(WaitingStatus newStatus, Waiting waiting) {
        WaitingStatus currentStatus = waiting.getStatus();

        boolean updated = false;

        // 상태 변경 가능 여부 확인 (역행 방지)
        boolean canAdvance = statusOrder.indexOf(newStatus) > statusOrder.indexOf(currentStatus);

        // 접수 -> 대기 (사장 권한)
        if (newStatus == WaitingStatus.WAITING && currentStatus == WaitingStatus.REQUESTED && canAdvance) {
            handleWaiting(waiting);
            updated = true;
        }

        // 대기 -> 호출 (사장 권한)
        else if (newStatus == WaitingStatus.CALLED && currentStatus == WaitingStatus.WAITING && canAdvance) {
            handleCalled(waiting);
            updated = true;
        }

        // 접수 -> 취소 (사장, 유저 둘다 가능)
        else if (newStatus == WaitingStatus.CANCELLED && currentStatus == WaitingStatus.REQUESTED && canAdvance) {
            handleRequestedToCancelled(waiting);
            updated = true;
        }

        // 대기 -> 취소 (사장, 유저 둘다 가능)
        else if (newStatus == WaitingStatus.CANCELLED && currentStatus == WaitingStatus.WAITING && canAdvance) {
            handleWaitingToCancelled(waiting);
            updated = true;
        }

        // 호출 -> 취소 (사장, 유저 둘다 가능)
        else if (newStatus == WaitingStatus.CANCELLED && currentStatus == WaitingStatus.CALLED && canAdvance) {
            handleCalledToCancelled(waiting);
            updated = true;
        }

        // 호출 -> 완료 (사장 권한)
        else if (newStatus == WaitingStatus.COMPLETED && currentStatus == WaitingStatus.CALLED && canAdvance) {
            handleCompleted(waiting);
            updated = true;
        }

        if (!updated) {
            throw new CustomException(ExceptionType.INVALID_WAITING_STATUS_UPDATE,
                    String.format("현재 상태: %s, 요청 상태: %s", currentStatus, newStatus));
        }

        return updated;
    }

    // REQUESTED -> WAITING: 가게 웨이팅 팀 수 + 1, 추가된 사용자 순서: 가게 웨이팅 팀 수 + 1, 제트셋에 추가
    private void handleWaiting(Waiting waiting) {
        // 변경된 상태를 DB에 먼저 저장
        Long storeId = waiting.getStore().getId();
        waiting.waiting(getCurrentTime());
        waitingRepository.save(waiting);

        String key = waitingRedisService.generateKey(storeId);
        waitingRedisService.addToWaitingZSet(key, waiting.getId(), waiting.getActivatedAt());

        int count = waitingRedisService.getWaitingCount(key);
        log.info("가게 {} 웨이팅 팀 추가됨 (Redis Key: {}): {} (현재 대기열 크기: {})", storeId, key, waiting.getId(), count);
    }

    // WAITING -> CALLED: 가게의 웨이팅 팀 수 - 1, 호출된 사용자의 웨이팅 순서 0, 제트셋에서 제거
    private void handleCalled(Waiting waiting) {
        // 호출 전 유효성 검증 (첫 번째 순서인지)
        validateCanBeCalled(waiting);
        Long storeId = waiting.getStore().getId();
        Long waitingId = waiting.getId();

        // 상태 변경
        waiting.call(getCurrentTime());
        waiting.markAsCalled();
        waitingRepository.save(waiting);

        // 제트셋에서 제거
        String key = waitingRedisService.generateKey(storeId);
        applicationEventPublisher.publishEvent(WaitingCalledEvent.from(waiting));
        waitingRedisService.removeFromWaitingZSet(key, waitingId);

        int count = waitingRedisService.getWaitingCount(key);
        log.info("가게 {} 웨이팅 팀 호출됨 (Redis Key: {}): {} (현재 대기열 크기: {})", storeId, key, waitingId, count);
    }

    // REQUESTED -> CANCELLED: 가게 웨이팅 팀 수 유지, 최소된 사용자의 웨이팅 순서 유지, 제트셋에 영향 없음
    private void handleRequestedToCancelled(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        waiting.cancel(getCurrentTime());
        waitingRepository.save(waiting);
        log.info("가게 {} 결제 전 웨이팅 요청 취소됨: {}", storeId, waiting.getId());
    }

    // WAITING -> CANCELLED: 가게 웨이팅 팀 수 - 1, 최소된 사용자의 웨이팅 순서 유지, 제트셋에서 제거
    private void handleWaitingToCancelled(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        Long waitingId = waiting.getId();

        // 상태 변경
        waiting.cancel(getCurrentTime());
        waitingRepository.save(waiting);

        // 제트셋에서 제거
        String key = waitingRedisService.generateKey(storeId);
        waitingRedisService.removeFromWaitingZSet(key, waitingId);
        log.info("가게 {} 대기 중인 웨이팅 취소됨 (Redis Key: {}): {}", storeId, key, waitingId);
    }

    // CALLED -> CANCELLED: 가게 웨이팅 팀 수 유지, 최소된 사용자의 웨이팅 순서 유지, 제트셋에 영향 없음
    private void handleCalledToCancelled(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        waiting.cancel(getCurrentTime());
        waitingRepository.save(waiting);
        log.info("가게 {} 호출 상태인 웨이팅 취소됨: {}", storeId, waiting.getId());
    }

    // CALLED -> COMPLETED: 가게 웨이팅 팀 수 유지, 입장한 사용자의 웨이팅 순서 0, 제트셋에 영향 없음
    private void handleCompleted(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        waiting.enter(getCurrentTime());
        waiting.markAsCalled();
        waitingRepository.save(waiting);
        log.info("가게 {} 웨이팅 완료됨: {}", storeId, waiting.getId());
    }

    // 호출 전 유효성 검증 (첫 번째 순서인지)
    private void validateCanBeCalled(Waiting waiting) {
        String key = waitingRedisService.generateKey(waiting.getStore().getId());
        if (!waitingRedisService.isFirstWaiting(key, waiting.getId())) {
            throw new CustomException(ExceptionType.NOT_FIRST_IN_WAITING_QUEUE);
        }
    }

    // 가게의 대기 상태인 웨이팅 팀 수
    @Override
    public int getCurrentWaitingTeamCount(Long storeId) {
        String key = waitingRedisService.generateKey(storeId);
        return waitingRedisService.getWaitingCount(key);
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }

    private void handleLockingExceptions(Exception e) {
        if (e instanceof LockTimeoutException) {
            log.warn("LockTimeoutException 발생: {}", e.getMessage());
            throw new CustomException(ExceptionType.LOCK_TIMEOUT);
        } else if (e instanceof PessimisticLockException) {
            log.error("PessimisticLockException 발생: {}", e.getMessage());
            throw new CustomException(ExceptionType.LOCK_FAILED);
        } else if (e instanceof DataAccessException) {
            log.error("DataAccessException 발생: {}", e.getMessage());
            throw new CustomException(ExceptionType.DATABASE_ERROR);
        }
    }

}
