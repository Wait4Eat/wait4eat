package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.dto.response.MyWaitingResponse;
import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.wait4eat.domain.waiting.entity.QWaiting.waiting;

@Repository
@RequiredArgsConstructor
public class WaitingQueryRepositoryImpl implements WaitingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<WaitingResponse> findWaitingsByStoreId(Long storeId, WaitingStatus status, Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(waiting.store.id.eq(storeId));

        if (status != null) {
            builder.and(waiting.status.eq(status));
        }

        List<Tuple> rows = queryFactory
                .select(
                        waiting.id,
                        waiting.store.id,
                        waiting.user.id,
                        waiting.peopleCount,
                        waiting.status,
                        waiting.createdAt,
                        waiting.calledAt,
                        waiting.cancelledAt,
                        waiting.enteredAt
                )
                .from(waiting)
                .where(builder)
                .orderBy(waiting.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<WaitingResponse> content = new ArrayList<>();
        for (Tuple row : rows) {
            WaitingResponse waitingResponse = WaitingResponse.builder()
                    .waitingId(row.get(waiting.id))
                    .storeId(row.get(waiting.store.id))
                    .userId(row.get(waiting.user.id))
                    .peopleCount(row.get(waiting.peopleCount))
                    .status(row.get(waiting.status))
                    .createdAt(row.get(waiting.createdAt))
                    .calledAt(row.get(waiting.calledAt))
                    .cancelledAt(row.get(waiting.cancelledAt))
                    .enteredAt(row.get(waiting.enteredAt))
                    .build();
            content.add(waitingResponse);
        }

        Long total = queryFactory
                .select(waiting.count())
                .from(waiting)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, Optional.ofNullable(total).orElse(0L));
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
                        waiting.waitingTeamCount,
                        waiting.myWaitingOrder,
                        waiting.createdAt,
                        waiting.calledAt,
                        waiting.cancelledAt,
                        waiting.enteredAt
                )
                .from(waiting)
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
                .status(row.get(waiting.status))
                .waitingTeamCount(row.get(waiting.waitingTeamCount))
                .myWaitingOrder(row.get(waiting.myWaitingOrder))
                .createdAt(row.get(waiting.createdAt))
                .calledAt(row.get(waiting.calledAt))
                .cancelledAt(row.get(waiting.cancelledAt))
                .enteredAt(row.get(waiting.enteredAt))
                .build();

        return Optional.of(waitingResponse);
    }
}
