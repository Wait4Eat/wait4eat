package com.example.wait4eat.global.message.outbox.repository;

import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OutboxJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private final String INSERT_SQL = """
            INSERT INTO outbox_messages
            (id, aggregate_type, aggregate_id, payload, status, retry_count, created_at)
            VALUES (?, ?, ?, ?, ?, ?, now())
            """;

    public void saveAll(List<OutboxMessage> outboxMessages) {
        jdbcTemplate.batchUpdate(INSERT_SQL, outboxMessages, 1000,
                ((ps, msg) -> {
                    ps.setString(1, msg.getId());
                    ps.setString(2, msg.getAggregateType());
                    ps.setLong(3, msg.getAggregateId());
                    ps.setString(4, msg.getPayload());
                    ps.setString(5, msg.getStatus().name());
                    ps.setInt(6, msg.getRetryCount());
                }
       ));
    }
}
