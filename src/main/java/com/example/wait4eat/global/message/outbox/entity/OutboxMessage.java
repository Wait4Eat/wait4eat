package com.example.wait4eat.global.message.outbox.entity;

import com.example.wait4eat.global.message.outbox.enums.OutboxMessageStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_messages",
        indexes = {
                @Index(name = "idx_status_created_at", columnList = "status, createdAt")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OutboxMessage {

    @Id
    @Column(length = 36, unique = true)
    private String id;

    @Column(nullable = false)
    private String aggregateType;

    @Column
    private Long aggregateId;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxMessageStatus status = OutboxMessageStatus.PENDING;

    @Column(nullable = false)
    private Integer retryCount = 0;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @Builder
    public OutboxMessage(String id, String aggregateType, Long aggregateId, String payload) {
        this.id = id;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxMessageStatus.PENDING;
        this.retryCount = 0;
    }

    public void markAsSent() {
        this.status = OutboxMessageStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = OutboxMessageStatus.FAILED;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
