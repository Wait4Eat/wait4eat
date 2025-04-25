package com.example.wait4eat.domain.store.repository;

import lombok.RequiredArgsConstructor;
import com.example.wait4eat.domain.store.entity.Store;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoreBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final int BATCH_SIZE = 1000;

    public void bulkInsert(List<Store> stores) {
        String sql = """
            INSERT INTO stores (
                user_id, name, address, open_time, close_time,
                description, deposit_amount, waiting_team_count,
                created_at, modified_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, stores, BATCH_SIZE, (ps, store) -> {
            ps.setLong(1, store.getUser().getId());
            ps.setString(2, store.getName());
            ps.setString(3, store.getAddress());
            ps.setTime(4, Time.valueOf(store.getOpenTime()));
            ps.setTime(5, Time.valueOf(store.getCloseTime()));
            ps.setString(6, store.getDescription());
            ps.setInt(7, store.getDepositAmount());
            ps.setTimestamp(9, Timestamp.valueOf(
                    store.getCreatedAt() != null ? store.getCreatedAt() : LocalDateTime.now()));
            ps.setTimestamp(10, Timestamp.valueOf(
                    store.getModifiedAt() != null ? store.getModifiedAt() : LocalDateTime.now()));
        });
    }
}
