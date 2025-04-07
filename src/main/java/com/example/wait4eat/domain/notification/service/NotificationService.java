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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        User user = getUserById(userId);

        return notificationRepository.findAllByUser(user, pageable)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));
    }


}
