package com.example.wait4eat.global.message.dto;

import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.global.message.enums.MessageType;
import lombok.Getter;

import java.util.List;

@Getter
public class NotificationMessagePublishRequest extends MessagePublishRequest {

    private List<User> targetUsers;
    private NotificationType notificationType;

    public NotificationMessagePublishRequest(
            MessageType type,
            String message,
            List<User> targetUsers,
            NotificationType notificationType
    ) {
        super(type, message);
        this.targetUsers = targetUsers;
        this.notificationType = notificationType;
    }
}
