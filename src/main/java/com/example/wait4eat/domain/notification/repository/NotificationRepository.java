package com.example.wait4eat.domain.notification.repository;

import com.example.wait4eat.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
