package com.example.wait4eat.domain.waiting.service;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.waiting.dto.request.UpdateWaitingRequest;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.domain.waiting.repository.WaitingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Slf4j
@SpringBootTest
public class WaitingPerformanceTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private WaitingRedisService waitingRedisService;

    @Autowired
    private WaitingService waitingService;

    private static final Long STORE_ID = 3L;
    private static final int NUMBER_OF_WAITINGS = 10;
    private Long ownerId;
    private Store store;
    private List<Waiting> createdWaitings = new ArrayList<>();

    @Transactional
    @Rollback(false)
    @BeforeEach
    void setupTestData() {
        store = storeRepository.findById(STORE_ID)
                .orElseGet(() -> {
                    User owner = userRepository.save(User.builder()
                            .email("owner1@test.com")
                            .nickname("owner1")
                            .password(passwordEncoder.encode("Owner1234!"))
                            .role(UserRole.ROLE_OWNER)
                            .build());
                    ownerId = owner.getId();
                    Store newStore = storeRepository.save(Store.builder()
                            .user(owner)
                            .name("Store3")
                            .address("서울특별시 도봉구 도봉로150길 42 (방학동, 삼성래미안아파트2차)")
                            .openTime(LocalTime.of(9, 30))
                            .closeTime(LocalTime.of(20, 30))
                            .description("Test Store3")
                            .depositAmount(10000)
                            .build());
                    log.info("생성된 가게 ID: {}", newStore.getId());
                    return newStore;
                });
        ownerId = store.getUser().getId();
        log.info("사용할 가게 ID: {}", store.getId());

        List<User> users = new ArrayList<>();
        for (int i = 1; i <= NUMBER_OF_WAITINGS; i++) {
            users.add(userRepository.save(User.builder()
                    .email("user" + i + "@test.com")
                    .nickname("user" + i)
                    .password(passwordEncoder.encode("User1234!"))
                    .role(UserRole.ROLE_USER)
                    .build()));
        }

        List<Waiting> requestedWaitings = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_WAITINGS; i++) {
            Waiting waiting = waitingRepository.save(Waiting.builder()
                    .store(store)
                    .user(users.get(i))
                    .orderId(UUID.randomUUID().toString())
                    .peopleCount(i + 1)
                    .myWaitingOrder(0)
                    .status(WaitingStatus.REQUESTED)
                    .build());
            requestedWaitings.add(waiting);
            createdWaitings.add(waiting); // 생성된 웨이팅 객체 리스트에 추가
            log.info("생성된 웨이팅 ID - 가게 ID: {}: {}", store.getId(), waiting.getId());
        }

        for (Waiting waiting : requestedWaitings) {
            UpdateWaitingRequest updateRequest = UpdateWaitingRequest.from(WaitingStatus.WAITING);
            waitingService.updateWaitingStatus(ownerId, waiting.getId(), updateRequest);
            log.info("웨이팅 상태 변경 시도 - 가게 ID: {}: WaitingId={}, Status=WAITING", store.getId(), waiting.getId());
        }
    }

    @Test
    void measureWaitingTime() {
        log.info("가게 ID: {}", store.getId()); // 생성된 store 객체의 ID 사용

        // DB로 웨이팅 조회 (레디스 적용 전: #66)
//        long dbStartTime = System.nanoTime();
//        int dbWaitingCount = waitingRepository.countByStoreIdAndStatus(store.getId(), WaitingStatus.WAITING);
//        long dbEndTime = System.nanoTime();
//        log.info("DB 웨이팅 조회 시간 (개수): {} ms", (dbEndTime - dbStartTime) / 1_000_000.0);
//        log.info("DB 웨이팅 개수: {}", dbWaitingCount);

        // 레디스로 웨이팅 조회 (기존 방식 유지 - DB ID 사용)
        long redisStartTime = System.nanoTime();
        String redisKey = waitingRedisService.generateKey(store.getId()); // 생성된 store 객체의 ID 사용
        int redisWaitingCount = waitingRepository.countByStoreIdAndStatus(store.getId(), WaitingStatus.WAITING);
        long redisEndTime = System.nanoTime();
        log.info("레디스 키: {}", redisKey);
        log.info("레디스 웨이팅 조회 시간: {} ms", (redisEndTime - redisStartTime) / 1_000_000.0);
        log.info("레디스 웨이팅 개수: {}", redisWaitingCount);
    }

}