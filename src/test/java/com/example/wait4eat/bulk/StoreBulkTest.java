package com.example.wait4eat.bulk;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreBulkRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
public class StoreBulkTest {

    @Autowired
    private StoreBulkRepository storeBulkRepository;

    @Autowired
    private UserRepository userRepository;

    private static final int TOTAL_STORES = 100000;
    private static final int BATCH_SIZE = 1000;

    @Test
    public void bulkInsertStores() {
        Random random = new Random();

        // 테스트용 user 가져오기 (없으면 생성)
        User user = userRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    User newUser = new User("test@example.com", "password", "tester", UserRole.ROLE_OWNER);
                    return userRepository.save(newUser);
                });

        for (int i = 0; i < TOTAL_STORES; i += BATCH_SIZE) {
            List<Store> stores = new ArrayList<>();

            for (int j = 0; j < BATCH_SIZE; j++) {
                String name = "가게-" + UUID.randomUUID().toString().substring(0, 8);
                String address = "서울시 랜덤구 " + random.nextInt(1000);
                LocalTime openTime = LocalTime.of(random.nextInt(24), random.nextInt(60));
                LocalTime closeTime = openTime.plusHours(random.nextInt(6) + 1).withMinute(random.nextInt(60));
                String description = "이것은 설명입니다. " + random.nextInt(1000);
                int depositAmount = (random.nextInt(10) + 1) * 1000;
                int waitingTeamCount = random.nextInt(30);

                Store store = Store.builder()
                        .user(user)
                        .name(name)
                        .address(address)
                        .openTime(openTime)
                        .closeTime(closeTime)
                        .description(description)
                        .depositAmount(depositAmount)
                        .waitingTeamCount(waitingTeamCount)
                        .build();

                stores.add(store);
            }

            storeBulkRepository.bulkInsert(stores);
        }
    }
}
