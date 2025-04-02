package com.example.wait4eat.domain.notification.entity;

import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String text;

    private Boolean isRead;

    @Builder
    public Notification(User user, NotificationType type, String text) {
        this.user = user;
        this.type = type;
        this.text = text;
        isRead = false;
    }
}
