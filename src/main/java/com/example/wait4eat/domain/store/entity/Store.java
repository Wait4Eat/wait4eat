package com.example.wait4eat.domain.store.entity;

import com.example.wait4eat.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "stores")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;

    private String address;

    private LocalTime openTime;

    private LocalTime closeTime;

    private String description;

    private String imageUrl;

    private int depositAmount;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Builder
    public Store(
            User user,
            String name,
            String address,
            LocalTime openTime,
            LocalTime closeTime,
            String description,
            String imageUrl,
            int depositAmount
    ) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description;
        this.imageUrl = imageUrl;
        this.depositAmount = depositAmount;
    }
}
