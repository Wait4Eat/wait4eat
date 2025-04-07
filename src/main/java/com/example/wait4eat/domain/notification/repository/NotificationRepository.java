package com.example.wait4eat.domain.notification.repository;

import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByUser(User user, Pageable pageable);
}
