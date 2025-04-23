package com.example.wait4eat.domain.notification.service;

import com.example.wait4eat.global.message.payload.NotificationPayload;
import com.example.wait4eat.domain.notification.dto.response.NotificationResponse;
import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.notification.repository.NotificationJdbcRepository;
import com.example.wait4eat.domain.notification.repository.NotificationRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import com.example.wait4eat.global.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationJdbcRepository  notificationJdbcRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification create(Long userId, NotificationType notificationType, String message) {
        User user =  getUserById(userId);

        return notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .type(notificationType)
                        .text(message)
                        .build()
        );
    }

    @Transactional
    public List<Notification> createBulk(List<User> users, NotificationType type, String message) {
        List<Notification> notifications = users.stream()
                .map(user -> Notification.builder()
                        .user(user)
                        .type(type)
                        .text(message)
                        .build())
                .toList();

        notificationJdbcRepository.saveAll(notifications);
        return notifications;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Boolean isRead, Pageable pageable) {
        User user = getUserById(userId);

        return notificationRepository.findAllByUserAndOptionalIsRead(user, isRead, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = getNotificationById(notificationId);
        validateNotificationBelongsToUser(notification, userId);
        notification.markAsRead();
    }

    private void validateNotificationBelongsToUser(Notification notification, Long userId) {
        if (!notification.getUser().getId().equals(userId)) {
            throw new CustomException(ExceptionType.NO_PERMISSION_ACTION);
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));
    }

    private Notification getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ExceptionType.NOTIFICATION_NOT_FOUND));
    }
}
