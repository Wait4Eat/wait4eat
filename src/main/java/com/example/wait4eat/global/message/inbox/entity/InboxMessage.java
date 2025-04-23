package com.example.wait4eat.global.message.inbox.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inbox_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InboxMessage {

    @Id
    @Column(length = 36, unique = true)
    private String id; // SQS 메시지 ID

    @CreatedDate
    private LocalDateTime receivedAt;

    public InboxMessage(String id) {
        this.id = id;
    }
}
