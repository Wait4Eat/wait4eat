package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.dto.response.MyPastWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.MyWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.wait4eat.domain.user.entity.QUser.user;
import static com.example.wait4eat.domain.store.entity.QStore.store;
import static com.example.wait4eat.domain.waiting.entity.QWaiting.waiting;

@Repository
@RequiredArgsConstructor
public class WaitingQueryRepositoryImpl implements WaitingQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final RedisTemplate<String, Long> waitingIdRedisTemplate; // Long 타입 RedisTemplate
    private static final String WAITING_STORE_KEY_PREFIX = "waiting:store:";
    private static final String WAITING_DATE_FORMAT = "yyyyMMdd";

    // 현재 날짜를 기준으로 Redis Key 생성
    private String generateWaitingStoreKey(Long storeId) {
        LocalDate today = LocalDate.now();
        return WAITING_STORE_KEY_PREFIX + storeId + ":" + today.format(DateTimeFormatter.ofPattern(WAITING_DATE_FORMAT));
    }

    // 특정 가게의 주어진 상태에 해당하는 웨이팅 수 조회
    @Override
    public int countByStoreIdAndStatus(Long storeId, WaitingStatus status) {

        // WAITING 상태에 대한 카운트는 Sorted Set의 크기
        if (status == WaitingStatus.WAITING) {
            Long size = waitingIdRedisTemplate.opsForZSet().zCard(generateWaitingStoreKey(storeId));
            return size != null ? size.intValue() : 0;
        }

        // 다른 상태에 대한 카운트는 DB에서 조회
        Integer count = queryFactory
                .select(waiting.count().intValue())
                .from(waiting)
                .where(
                        waiting.store.id.eq(storeId),
                        waiting.status.eq(status)
                )
                .fetchOne();

        return Optional.ofNullable(count).orElse(0);
    }

    // 매장 ID와 웨이팅 ID를 사용하여 대기 번호 조회
    @Override
    public Long getUserWaitingRank(Long storeId, Long waitingId) {

        boolean isMember = waitingIdRedisTemplate.opsForZSet()
                .rank(generateWaitingStoreKey(storeId), waitingId) != null;

        if (!isMember) {
            return null; // 이미 호출되었거나 대기열에 없음
        }

        Long rank = waitingIdRedisTemplate.opsForZSet().rank(generateWaitingStoreKey(storeId), waitingId);
        return rank != null ? rank + 1 : null; // Redis의 rank()는 0부터 시작하므로 +1 해서 순위 표시
    }

    @Override
    public Page<WaitingResponse> findWaitingsByStoreId(Long storeId, WaitingStatus status, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(waiting.store.id.eq(storeId));

        if (status != null) {
            builder.and(waiting.status.eq(status));
        }

        // 1. 대상 Waiting ID 목록 조회 (페이징 적용)
        List<Long> waitingIds= queryFactory
                .select(waiting.id)
                .distinct()
                .from(waiting)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (waitingIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. 해당 ID 목록에 해당하는 Waiting 엔티티 및 연관 엔티티 조회 (IN 절 사용)
        List<Waiting> waitings = queryFactory
                .selectFrom(waiting)
                .join(waiting.store, store).fetchJoin()
                .join(waiting.user, user).fetchJoin()
                .where(waiting.id.in(waitingIds))
                .fetch();

        // 3. 조회된 Waiting 엔티티 리스트를 WaitingResponse로 변환
        List<WaitingResponse> content = waitings.stream()
                .map(w -> {
                    Long rank = w.getStatus() == WaitingStatus.WAITING
                            ? getUserWaitingRank(w.getStore().getId(), w.getId())
                            : null;
                    return WaitingResponse.of(w, rank);
                })
                .collect(Collectors.toList());

        // 4. 페이징 처리를 위한 전체 개수 쿼리
        long total = Optional.ofNullable(queryFactory
                .select(waiting.count())
                .from(waiting)
                .where(builder)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<MyWaitingResponse> findMyWaiting(Long userId) {
        Waiting waitingResult = queryFactory
                .selectFrom(waiting)
                .join(waiting.store, store).fetchJoin()
                .join(waiting.user, user).fetchJoin()
                .where(waiting.user.id.eq(userId)
                        .and(waiting.status.in(WaitingStatus.REQUESTED, WaitingStatus.WAITING, WaitingStatus.CALLED)))
                .fetchOne();

        // Redis ZSet: Size -> waitingTeamCount, Rank -> myWaitingOrder
        if (waitingResult != null) {
            Long storeId = waitingResult.getStore().getId();
            Long waitingId = waitingResult.getId();

            int currentWaitingTeamCount = countByStoreIdAndStatus(storeId, WaitingStatus.WAITING);
            Long rank = getUserWaitingRank(storeId, waitingId);
            Integer myWaitingOrder = (rank != null) ? rank.intValue() : null;

            return Optional.of(MyWaitingResponse.of(waitingResult, currentWaitingTeamCount, myWaitingOrder));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Page<MyPastWaitingResponse> findMyPastWaitings(Long userId, Pageable pageable) {
        // 1. 대상 Waiting ID 전부 조회
        List<Long> waitingIds = queryFactory
                .select(waiting.id)
                .distinct()
                .from(waiting)
                .where(
                        waiting.user.id.eq(userId),
                        waiting.status.in(WaitingStatus.CANCELLED, WaitingStatus.COMPLETED)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (waitingIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. Waiting ID 목록으로 필요한 데이터 한 번에 조회 (Store, User 포함)
        List<Waiting> waitings = queryFactory
                .selectFrom(waiting)
                .join(waiting.store, store).fetchJoin()
                .join(waiting.user, user).fetchJoin()
                .where(waiting.id.in(waitingIds))
                .fetch();

        List<MyPastWaitingResponse> content = waitings.stream()
                .map(MyPastWaitingResponse::from)
                .collect(Collectors.toList());

        // 3. 페이징 처리를 위한 전체 개수 쿼리
        long total = Optional.ofNullable(queryFactory
                .select(waiting.count())
                .from(waiting)
                .where(
                        waiting.user.id.eq(userId),
                        waiting.status.in(WaitingStatus.CANCELLED, WaitingStatus.COMPLETED)
                )
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
