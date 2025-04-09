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
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateWaitingResponse createWaiting(Long userId, Long storeId, CreateWaitingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));

        // 현재 사용자의 활성 웨이팅 상태 확인
        waitingRepository.findByUserIdAndStatus(userId, WaitingStatus.WAITING)
                .ifPresent(waiting -> {
                    throw new CustomException(ExceptionType.SINGLE_WAIT_ALLOWED);
                });

        // 현재 가게의 총 웨이팅 팀 수 조회
        int currentTotalWaitingTeamCount = waitingRepository.countByStoreIdAndStatus(storeId, WaitingStatus.WAITING);

        // 고유한 주문 ID 생성 (UUID 사용)
        String orderId = UUID.randomUUID().toString();

        // 새로운 웨이팅 생성
        int myWaitingOrder = currentTotalWaitingTeamCount + 1;
        Waiting waiting = Waiting.builder()
                .store(store)
                .user(user)
                .orderId(orderId) // 주문 ID 저장 (UUID)
                .peopleCount(request.getPeopleCount())
                .myWaitingOrder(myWaitingOrder) // DB에 저장되는 순번
                .status(WaitingStatus.WAITING)
                .build();

        Waiting savedWaiting = waitingRepository.save(waiting);

        // 스토어의 전체 웨이팅 팀 수 증가 및 저장
        store.incrementWaitingTeamCount();
        storeRepository.save(store);

        return CreateWaitingResponse.from(savedWaiting);
    }

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

    @Transactional(readOnly = true)
    public MyWaitingResponse getMyWaiting(Long userId) {
        return waitingRepository.findMyWaiting(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.NO_CURRENT_WAITING));
    }

    @Transactional(readOnly = true)
    public Page<MyPastWaitingResponse> getMyPastWaitings(Long userId, Pageable pageable) {
        return waitingRepository.findMyPastWaitings(userId, pageable);
    }

    // 사용자가 웨이팅 취소
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

        // 가게의 웨이팅 팀 수 감소
        Store store = waiting.getStore();
        store.decrementWaitingTeamCount();

        // 취소한 사용자의 웨이팅 순서 유지
        // 웨이팅 상태만 변경하고 취소 시간 기록
        waiting.cancel(getCurrentTime());
        reorderWaitingQueue(waiting.getStore().getId()); // 전체 재정렬 호출

        return CancelWaitingResponse.from("웨이팅이 성공적으로 취소되었습니다.");
    }

    @Transactional
    public UpdateWaitingResponse updateWaitingStatus(Long userId, Long waitingId, UpdateWaitingRequest updateWaitingRequest) {
        Waiting waiting = waitingRepository.findById(waitingId)
                .orElseThrow(() -> new CustomException(ExceptionType.WAITING_NOT_FOUND));

        // 가게 주인 확인 로직
        if (!waiting.getStore().getUser().getId().equals(userId)) {
            throw new CustomException(ExceptionType.NO_PERMISSION_ACTION);
        }

        WaitingStatus newStatus = updateWaitingRequest.getStatus();

        // 사장님 웨이팅 팀 호출
        if (newStatus == WaitingStatus.CALLED && waiting.getStatus() == WaitingStatus.WAITING) {
            // 가게의 웨이팅 팀 수 유지
            // 호출된 사용자의 웨이팅 순서 0
            waiting.call(getCurrentTime());
            waiting.markAsCalled();
        }

        // 사장님 웨이팅 개별 취소
        else if (newStatus == WaitingStatus.CANCELLED && waiting.getStatus() != WaitingStatus.CANCELLED) {
            waiting.cancel(getCurrentTime());
            // 가게 웨이팅 팀 수 감소
            // 최소된 사용자의 웨이팅 순서 유지
            Store store = waiting.getStore();
            store.decrementWaitingTeamCount();

            reorderWaitingQueue(waiting.getStore().getId()); // 전체 재정렬 호출
        }

        // 웨이팅 팀이 가게로 입장 완료
        else if (newStatus == WaitingStatus.COMPLETED && waiting.getStatus() != WaitingStatus.COMPLETED) {
            waiting.enter(getCurrentTime());
            // 가게 웨이팅 팀 수 감소
            // 입장한 사용자의 웨이팅 순서 0
            waiting.markAsCalled();
            Store store = waiting.getStore();
            store.decrementWaitingTeamCount();
            reorderWaitingQueue(waiting.getStore().getId()); // 전체 재정렬 호출
        }

        return UpdateWaitingResponse.from(waiting);
    }

    private LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }

    private void reorderWaitingQueue(Long storeId) {
        List<Waiting> waitingList = waitingRepository.findByStoreIdAndStatusOrderByCreatedAtAsc(storeId, WaitingStatus.WAITING);
        int order = 1;
        for (Waiting waiting : waitingList) {
            waiting.updateMyWaitingOrder(order++);
        }
    }
}
