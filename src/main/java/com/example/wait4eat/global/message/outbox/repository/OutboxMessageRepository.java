package com.example.wait4eat.global.message.outbox.repository;

import com.example.wait4eat.global.message.outbox.entity.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, String> {

    @Query(value = """
    SELECT * FROM outbox_messages
    WHERE status IN ('FAILED', 'PENDING')
    AND retry_count < :maxRetry
    ORDER BY created_at ASC
    LIMIT :limit
    """, nativeQuery = true)
    List<OutboxMessage> findRetryableOutboxMessages(int maxRetry, int limit);


    @Modifying
    @Query("UPDATE OutboxMessage o SET o.status = 'SENT', o.sentAt = :now WHERE o.id IN :ids")
    int markAllAsSent(List<String> ids, LocalDateTime now);

    @Modifying
    @Query("UPDATE OutboxMessage o SET o.status = 'FAILED' WHERE o.id IN :ids")
    int markAllAsFailed(List<String> ids);

   // boolean existsByIdAndIsProcessed(String messageKey, boolean isProcessed);
}
