package com.example.wait4eat.domain.notification.service;

import com.example.wait4eat.domain.notification.dto.response.NotificationResponse;
import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.notification.enums.NotificationType;
import com.example.wait4eat.domain.notification.repository.NotificationRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification create(Long userId, NotificationType notificationType) {
        User user =  getUserById(userId);

        return notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .type(notificationType)
                        .text(notificationType.getMessage())
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        User user = getUserById(userId);

        return notificationRepository.findAllByUser(user, pageable)
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
