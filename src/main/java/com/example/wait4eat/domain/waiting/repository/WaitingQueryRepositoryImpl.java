package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.dto.response.MyPastWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.MyWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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

    @Override
    public int countByStoreIdAndStatus(Long storeId, WaitingStatus status) {
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

    @Override
    public Page<WaitingResponse> findWaitingsByStoreId(Long storeId, WaitingStatus status, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(waiting.store.id.eq(storeId));

        if (status != null) {
            builder.and(waiting.status.eq(status));
        }

        List<Waiting> waitings = queryFactory
                .select(waiting)
                .from(waiting)
                .join(waiting.store, store).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<WaitingResponse> content = waitings.stream()
                .map(waiting -> WaitingResponse.builder()
                        .waitingId(waiting.getId())
                        .storeId(waiting.getStore().getId())
                        .userId(waiting.getUser().getId())
                        .peopleCount(waiting.getPeopleCount())
                        .waitingTeamCount(waiting.getStore().getWaitingTeamCount())
                        .myWaitingOrder(waiting.getMyWaitingOrder())
                        .status(waiting.getStatus())
                        .createdAt(waiting.getCreatedAt())
                        .calledAt(waiting.getCalledAt())
                        .cancelledAt(waiting.getCancelledAt())
                        .enteredAt(waiting.getEnteredAt())
                        .build())
                .collect(Collectors.toList());

        Long total = Optional.ofNullable(queryFactory
                .select(waiting.count())
                .from(waiting)
                .where(builder)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<MyWaitingResponse> findMyWaiting(Long userId) {
        Tuple row = queryFactory
                .select(
                        waiting.id,
                        waiting.store.id,
                        waiting.user.id,
                        waiting.peopleCount,
                        waiting.status,
                        store.waitingTeamCount,
                        waiting.myWaitingOrder,
                        waiting.createdAt,
                        waiting.calledAt,
                        waiting.cancelledAt,
                        waiting.enteredAt
                )
                .from(waiting)
                .join(waiting.store, store)
                .where(waiting.user.id.eq(userId), waiting.status.eq(WaitingStatus.WAITING))
                .fetchOne();

        if (row == null) {
            return Optional.empty();
        }

        MyWaitingResponse waitingResponse = MyWaitingResponse.builder()
                .waitingId(row.get(waiting.id))
                .storeId(row.get(waiting.store.id))
                .userId(row.get(waiting.user.id))
                .peopleCount(row.get(waiting.peopleCount))
                .waitingTeamCount(row.get(store.waitingTeamCount))
                .myWaitingOrder(row.get(waiting.myWaitingOrder))
                .status(row.get(waiting.status))
                .createdAt(row.get(waiting.createdAt))
                .calledAt(row.get(waiting.calledAt))
                .cancelledAt(row.get(waiting.cancelledAt))
                .enteredAt(row.get(waiting.enteredAt))
                .build();

        return Optional.of(waitingResponse);
    }

    @Override
    public Page<MyPastWaitingResponse> findMyPastWaitings(Long userId, Pageable pageable) {
        List<Waiting> waitings = queryFactory
                .selectFrom(waiting)
                .join(waiting.store, store).fetchJoin()
                .join(waiting.user, user).fetchJoin()  
                .where(
                        waiting.user.id.eq(userId),
                        waiting.status.in(WaitingStatus.CANCELLED, WaitingStatus.COMPLETED)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<MyPastWaitingResponse> content = waitings.stream()
                .map(MyPastWaitingResponse::from)
                .collect(Collectors.toList());

        Long total = Optional.ofNullable(queryFactory
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
