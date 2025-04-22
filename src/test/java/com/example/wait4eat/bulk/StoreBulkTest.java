package com.example.wait4eat.bulk;

import com.example.wait4eat.domain.store.dto.request.SearchStoreRequest;
import com.example.wait4eat.domain.store.dto.response.GetStoreListResponse;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.entity.StoreDocument;
import com.example.wait4eat.domain.store.repository.StoreBulkRepository;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.store.repository.StoreSearchRepository;
import com.example.wait4eat.domain.store.service.StoreService;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("test")
public class StoreBulkTest {

    @Autowired
    private StoreBulkRepository storeBulkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreService storeService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreSearchRepository storeSearchRepository;

    private static final int TOTAL_STORES = 100000;
    private static final int BATCH_SIZE = 1000;

//    @Test
//    public void bulkInsertStores() {
//        Random random = new Random();
//
//        // 테스트용 user 가져오기 (없으면 생성)
//        User user = userRepository.findAll().stream().findFirst()
//                .orElseGet(() -> {
//                    User newUser = new User("test@example.com", "password", "tester", UserRole.ROLE_OWNER);
//                    return userRepository.save(newUser);
//                });
//
//        for (int i = 0; i < TOTAL_STORES; i += BATCH_SIZE) {
//            List<Store> stores = new ArrayList<>();
//
//            for (int j = 0; j < BATCH_SIZE; j++) {
//                String name = "가게-" + UUID.randomUUID().toString().substring(0, 8);
//                String address = "서울시 랜덤구 " + random.nextInt(1000);
//                LocalTime openTime = LocalTime.of(random.nextInt(24), random.nextInt(60));
//                LocalTime closeTime = openTime.plusHours(random.nextInt(6) + 1).withMinute(random.nextInt(60));
//                String description = "이것은 설명입니다. " + random.nextInt(1000);
//                int depositAmount = (random.nextInt(10) + 1) * 1000;
//                int waitingTeamCount = random.nextInt(30);
//
//                Store store = Store.builder()
//                        .user(user)
//                        .name(name)
//                        .address(address)
//                        .openTime(openTime)
//                        .closeTime(closeTime)
//                        .description(description)
//                        .depositAmount(depositAmount)
//                        .waitingTeamCount(waitingTeamCount)
//                        .build();
//
//                stores.add(store);
//            }
//            storeBulkRepository.bulkInsert(stores);
//        }
//    }

    @Test
    public void testBulkSyncToElasticsearch() {
        // 동기화 시작 시간 기록
        long startTime = System.currentTimeMillis();

        int page = 0;
        int size = BATCH_SIZE;
        Page<Store> storePage;

        do {
            Pageable pageable = PageRequest.of(page, size);
            storePage = storeRepository.findAll(pageable);

            System.out.println("Processing page " + page + ", total elements: " + storePage.getTotalElements() +
                    ", total pages: " + storePage.getTotalPages());

            try {
                List<StoreDocument> documents = storePage.getContent().stream()
                        .map(StoreDocument::from)
                        .collect(Collectors.toList());
                storeSearchRepository.saveAll(documents);
                System.out.println("Synced " + documents.size() + " stores to Elasticsearch, page " + page);
            } catch (Exception e) {
                System.err.println("Error syncing stores to Elasticsearch, page " + page + ": " + e.getMessage());
            }

            page++;
        } while (storePage.hasNext());

        // 동기화 종료 시간 기록
        long endTime = System.currentTimeMillis();
        System.out.println("ES 동기화 시간: " + (endTime - startTime) + "ms");
    }


    @Test
    public void testJpaSearchPerformance() {
        // 검색 요청 생성
        SearchStoreRequest request = SearchStoreRequest.builder()
                .name("가게") // 가게 이름으로 검색 (대용량 데이터에 포함된 패턴)
                .address("서울") // 주소로 검색
                .description("설명") // 설명으로 검색
                .openTime(LocalTime.of(9, 0)) // 오픈 시간 필터
                .closeTime(LocalTime.of(22, 0)) // 마감 시간 필터
                .page(0) // 첫 페이지
                .size(10) // 페이지당 10개
                .sort("createdAt") // 생성일 기준 정렬
                .sortDirection("desc") // 내림차순
                .build();

        // 검색 실행 및 응답 시간 측정
        long startTime = System.currentTimeMillis();
        Page<GetStoreListResponse> result = storeService.getStoreList(request);
        long endTime = System.currentTimeMillis();

        // 결과 출력
        System.out.println("JPA 검색 시간: " + (endTime - startTime) + "ms");
        System.out.println("검색 결과 수: " + result.getTotalElements());
        System.out.println("페이지 수: " + result.getTotalPages());
    }

    @Test
    public void testJpaSearchPerformanceMultipleConditions() {
        // 다양한 검색 조건 테스트
        SearchStoreRequest[] requests = {
                SearchStoreRequest.builder()
                        .name("가게")
                        .page(0)
                        .size(10)
                        .sort("createdAt")
                        .sortDirection("desc")
                        .build(),
                SearchStoreRequest.builder()
                        .address("서울")
                        .openTime(LocalTime.of(8, 0))
                        .closeTime(LocalTime.of(23, 0))
                        .page(0)
                        .size(20)
                        .sort("depositAmount")
                        .sortDirection("asc")
                        .build(),
                SearchStoreRequest.builder()
                        .description("설명")
                        .page(1)
                        .size(5)
                        .sort("waitingTeamCount")
                        .sortDirection("desc")
                        .build()
        };

        for (int i = 0; i < requests.length; i++) {
            SearchStoreRequest request = requests[i];
            long startTime = System.currentTimeMillis();
            Page<GetStoreListResponse> result = storeService.getStoreList(request);
            long endTime = System.currentTimeMillis();

            System.out.println("테스트 " + (i + 1) + ":");
            System.out.println("조건: name=" + request.getName() +
                    ", address=" + request.getAddress() +
                    ", description=" + request.getDescription() +
                    ", openTime=" + request.getOpenTime() +
                    ", closeTime=" + request.getCloseTime());
            System.out.println("JPA 검색 시간: " + (endTime - startTime) + "ms");
            System.out.println("검색 결과 수: " + result.getTotalElements());
            System.out.println("페이지 수: " + result.getTotalPages());
            System.out.println("--------------------");
        }
    }

    @Test
    public void testEsSearchPerformance() {
        SearchStoreRequest request = SearchStoreRequest.builder()
                .name("가게")
                .address("서울")
                .description("설명")
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .page(0)
                .size(10)
                .sort("createdAt")
                .sortDirection("desc")
                .build();

        long startTime = System.currentTimeMillis();
        Page<GetStoreListResponse> result = storeService.getStoreListByEs(request);
        long endTime = System.currentTimeMillis();

        System.out.println("ES 검색 시간: " + (endTime - startTime) + "ms");
        System.out.println("검색 결과 수: " + result.getTotalElements());
        System.out.println("페이지 수: " + result.getTotalPages());
    }
}
