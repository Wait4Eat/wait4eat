package com.example.wait4eat.domain.notification.repository;

import com.example.wait4eat.domain.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private final String INSERT_SQL = """
            INSERT INTO notifications
            (id, user_id, text, type, is_read, created_at)
            VALUES (?, ?, ?, ?, ?, now())
            """;

    public void saveAll(List<Notification> notifications) {
        jdbcTemplate.batchUpdate(INSERT_SQL, notifications, 1000,
                ((ps, notification) -> {
                    ps.setLong(1, notification.getId());
                    ps.setLong(2, notification.getUser().getId());
                    ps.setString(3, notification.getText());
                    ps.setString(4, notification.getType().name());
                    ps.setBoolean(5, notification.getIsRead());
                }
        ));
    }
}
