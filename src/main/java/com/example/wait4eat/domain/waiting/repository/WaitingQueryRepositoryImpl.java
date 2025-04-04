package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.dto.response.WaitingResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.wait4eat.domain.user.entity.QUser.user;
import static com.example.wait4eat.domain.waiting.entity.QWaiting.waiting;

@Repository
@RequiredArgsConstructor
public class WaitingQueryRepositoryImpl implements WaitingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<WaitingResponse> findWaitingsByStoreId(Long storeId, Pageable pageable) {
        // ... (이전 코드와 동일한 쿼리 구현)
        List<WaitingResponse> content = queryFactory
                .select(Projections.constructor(WaitingResponse.class,
                        waiting.store.id,
                        waiting.user.id,
                        waiting.peopleCount,
                        waiting.status,
                        waiting.createdAt,
                        waiting.calledAt,
                        waiting.cancelledAt,
                        waiting.enteredAt
                ))
                .from(waiting)
                .join(waiting.user, user) // N+1 문제 해결을 위한 JOIN
                .where(waiting.store.id.eq(storeId))
                .orderBy(waiting.createdAt.desc()) // 필요에 따라 정렬 조건 추가
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(waiting.count())
                .from(waiting)
                .where(waiting.store.id.eq(storeId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

}
