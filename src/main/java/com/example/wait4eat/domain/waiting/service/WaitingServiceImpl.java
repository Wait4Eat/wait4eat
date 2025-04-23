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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitingServiceImpl implements WaitingService {

    private final WaitingRepository waitingRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    private final RedisTemplate<String, Long> waitingIdRedisTemplate;
    private static final String WAITING_QUEUE_KEY_PREFIX = "waiting:queue:store:";


    // 웨이팅 상태 순서 정의
    private static final List<WaitingStatus> statusOrder = List.of(
            WaitingStatus.REQUESTED,
            WaitingStatus.WAITING,
            WaitingStatus.CALLED,
            WaitingStatus.CANCELLED,
            WaitingStatus.COMPLETED
    );

    @Override
    @Transactional
    public CreateWaitingResponse createWaiting(Long userId, Long storeId, CreateWaitingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));

        // 현재 사용자의 활성 웨이팅 상태 확인
        waitingRepository.findByUserIdAndStatusIn(userId, List.of(WaitingStatus.REQUESTED, WaitingStatus.WAITING, WaitingStatus.CALLED))
                .ifPresent(waiting -> {
                    throw new CustomException(ExceptionType.SINGLE_WAIT_ALLOWED);
                });

        // 고유한 주문 ID 생성 (UUID 사용)
        String orderId = UUID.randomUUID().toString();

        // 새로운 웨이팅 생성
        Waiting waiting = Waiting.builder()
                .store(store)
                .user(user)
                .orderId(orderId) // 주문 ID 저장 (UUID)
                .peopleCount(request.getPeopleCount())
                .myWaitingOrder(0) // 초기값 0 또는 다른 값으로 설정 (결제 후 업데이트)
                .status(WaitingStatus.REQUESTED)
                .build();

        Waiting savedWaiting = waitingRepository.save(waiting);

        return CreateWaitingResponse.from(savedWaiting);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WaitingResponse> getWaitings(Long userId, Long storeId, WaitingStatus status, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));

        // 해당 userId가 가게를 생성한 본인인지 확인
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
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

        if (!waiting.getUser().getId().equals(userId)) {
            throw new CustomException(ExceptionType.UNAUTHORIZED_CANCEL_WAITING);
        }

        if (waiting.getStatus() == WaitingStatus.CANCELLED || waiting.getStatus() == WaitingStatus.COMPLETED) {
            throw new CustomException(ExceptionType.ALREADY_FINISHED_WAITING);
        }

        updateWaiting(WaitingStatus.CANCELLED, waiting);

        return CancelWaitingResponse.from(waiting);
    }

    @Override
    @Transactional
    public UpdateWaitingResponse updateWaitingStatus(Long userId, Long waitingId, UpdateWaitingRequest updateWaitingRequest) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

        // 가게 주인 확인 로직
        if (!waiting.getStore().getUser().getId().equals(userId)) {
            throw new CustomException(ExceptionType.NO_PERMISSION_ACTION);
        }

        updateWaiting(updateWaitingRequest.getStatus(), waiting);

        return UpdateWaitingResponse.from(waiting);
    }

    @Override
    @Transactional
    public boolean updateWaiting(WaitingStatus newStatus, Waiting waiting) {
        WaitingStatus currentStatus = waiting.getStatus();

        boolean updated = false;

        // 상태 변경 가능 여부 확인 (역행 방지)
        boolean canAdvance = statusOrder.indexOf(newStatus) > statusOrder.indexOf(currentStatus);

        // 웨이팅 접수 -> 웨이팅 목록 추가
        if (newStatus == WaitingStatus.WAITING && currentStatus == WaitingStatus.REQUESTED && canAdvance) {
            handleWaiting(waiting);
            updated = true;
        }

        // 사장님 웨이팅 팀 호출
        else if (newStatus == WaitingStatus.CALLED && currentStatus == WaitingStatus.WAITING && canAdvance) {
            handleCalled(waiting);
            updated = true;
        }

        // 사장님 웨이팅 개별 취소 (REQUESTED -> CANCELLED)
        else if (newStatus == WaitingStatus.CANCELLED && currentStatus == WaitingStatus.REQUESTED && canAdvance) {
            handleRequestedToCancelled(waiting);
            updated = true;
        }

        // 사장님 웨이팅 개별 취소 (WAITING -> CANCELLED) & 유저가 본인 웨이팅 취소
        else if (newStatus == WaitingStatus.CANCELLED && currentStatus == WaitingStatus.WAITING && canAdvance) {
            handleWaitingToCancelled(waiting);
            updated = true;
        }

        // 사장님 웨이팅 개별 취소 (CALLED -> CANCELLED)
        else if (newStatus == WaitingStatus.CANCELLED && currentStatus == WaitingStatus.CALLED && canAdvance) {
            handleCalledToCancelled(waiting);
            updated = true;
        }

        // 웨이팅 팀이 가게로 입장 완료
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

    // REQUESTED -> WAITING: 가게 웨이팅 팀 수 증가, 호출된 사용자의 웨이팅 순서 + 1, 레디스 제트셋에 추가
    private void handleWaiting(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        waiting.waiting(getCurrentTime());
        waitingRepository.save(waiting);  // 현재 상태를 먼저 저장

        // Redis에 activatedAt을 밀리초로 변환한 값을 score로 넣어 저장
        waitingIdRedisTemplate.opsForZSet().add(
                WAITING_QUEUE_KEY_PREFIX + storeId,
                waiting.getId(),
                waiting.getActivatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()
        );

        int currentSize = waitingRepository.countByStoreIdAndStatus(storeId, WaitingStatus.WAITING);
        // int currentSize = getCurrentWaitingTeamCount(storeId);
        log.info("가게 {} 웨이팅 팀 추가됨: {} (현재 대기열 크기: {})", storeId, waiting.getId(), currentSize);
    }

    // WAITING -> CALLED: 가게의 웨이팅 팀 수 감소, 호출된 사용자의 웨이팅 순서 0, 레디스 제트셋에서 제거하고 재정렬 안해도 됨
    private void handleCalled(Waiting waiting) {
        validateCanBeCalled(waiting); // 호출 전 유효성 검증

        Long storeId = waiting.getStore().getId();
        Long waitingId = waiting.getId();

        // 상태 변경
        waiting.call(getCurrentTime());
        waiting.markAsCalled();
        int currentCount = getCurrentWaitingTeamCount(waiting.getStore().getId());
        log.info("가게 {} 웨이팅 팀 호출됨. 현재 웨이팅 팀 수: {}", waiting.getStore().getId(), currentCount);

        applicationEventPublisher.publishEvent(WaitingCalledEvent.from(waiting));

        waitingRepository.save(waiting);

        // Redis에서 제거
        waitingIdRedisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY_PREFIX + storeId, waitingId);
        log.info("가게 {} 웨이팅 팀 호출됨: {}", storeId, waitingId);
    }

    // REQUESTED -> CANCELLED: 가게 웨이팅 팀 수 그대로, 최소된 사용자의 웨이팅 순서 유지, 레디스에 영향 없음
    private void handleRequestedToCancelled(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        waiting.cancel(getCurrentTime());
        waitingRepository.save(waiting);
        log.info("가게 {} 웨이팅 요청 취소됨 (결제 전): {}", storeId, waiting.getId());
    }

    // WAITING -> CANCELLED: 가게 웨이팅 팀 수 감소, 최소된 사용자의 웨이팅 순서 유지, 레디스 제트셋에서 제거하고 재정렬 안해도 됨
    private void handleWaitingToCancelled(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        Long waitingId = waiting.getId();

        // 상태 변경
        waiting.cancel(getCurrentTime());
        waitingRepository.save(waiting);

        // Redis에서 제거
        waitingIdRedisTemplate.opsForZSet().remove(WAITING_QUEUE_KEY_PREFIX + storeId, waitingId);
        log.info("가게 {} 웨이팅 취소됨 (대기 중): {}", storeId, waitingId);
    }

    // CALLED -> CANCELLED: 가게 웨이팅 팀 수 그대로, 최소된 사용자의 웨이팅 순서, 유지 레디스에 영향 없음
    private void handleCalledToCancelled(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        waiting.cancel(getCurrentTime());
        waitingRepository.save(waiting);
        log.info("가게 {} 웨이팅 취소됨 (호출 상태): {}", storeId, waiting.getId());
    }

    // CALLED -> COMPLETED: 가게 웨이팅 팀 수 그대로, 입장한 사용자의 웨이팅 순서 0, 레디스에 영향 없음
    private void handleCompleted(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        waiting.enter(getCurrentTime());
        waiting.markAsCalled();
        waitingRepository.save(waiting);
        log.info("가게 {} 웨이팅 완료됨: {}", storeId, waiting.getId());
    }

    // 호출 전 유효성 체크: handleCalled() 안에서 Redis의 가장 앞 순서인지 검증
    private void validateCanBeCalled(Waiting waiting) {
        Long storeId = waiting.getStore().getId();
        Long waitingId = waiting.getId();

        // Redis에서 가장 앞 순서의 대기 ID 확인
        Set<Long> first = waitingIdRedisTemplate.opsForZSet()
                .range(WAITING_QUEUE_KEY_PREFIX + storeId, 0, 0);

        if (first == null || first.isEmpty() || !first.iterator().next().equals(waitingId)) {
            throw new CustomException(ExceptionType.NOT_FIRST_IN_WAITING_QUEUE);
        }
    }

    @Override
    // 가게의 현재 '대기 중'인 웨이팅팀 수
    public int getCurrentWaitingTeamCount(Long storeId) {
        Long size = waitingIdRedisTemplate.opsForZSet().zCard(WAITING_QUEUE_KEY_PREFIX + storeId);
        return size != null ? size.intValue() : 0;
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }

}
