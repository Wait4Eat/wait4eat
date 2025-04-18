package com.example.wait4eat.domain.notification.repository;

import com.example.wait4eat.domain.notification.entity.Notification;
import com.example.wait4eat.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(
            "SELECT n FROM Notification n WHERE n.user = :user "
                    + "AND (:isRead IS NULL OR n.isRead = :isRead)"
    )
    Page<Notification> findAllByUserAndOptionalIsRead(User user, Boolean isRead, Pageable pageable);
}
