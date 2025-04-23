package com.example.wait4eat.domain.notification.entity;

import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.global.util.IdGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    private String text;

    @Column(nullable = false)
    private Boolean isRead;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Notification(User user, NotificationType type, String text) {
        this.id = IdGenerator.generateNotificationId();
        this.user = user;
        this.type = type;
        this.text = text;
        isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
