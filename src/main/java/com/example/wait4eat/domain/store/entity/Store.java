package com.example.wait4eat.domain.store.entity;

import com.example.wait4eat.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Null;
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

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private int depositAmount;

    private int waitingTeamCount;

    @CreatedDate
    @Column(nullable = false)
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
            int depositAmount,
            int waitingTeamCount
    ) {
        this.user = user;
        this.name = name;
        this.address = address;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.description = description;
        this.imageUrl = imageUrl;
        this.depositAmount = depositAmount;
        this.waitingTeamCount = waitingTeamCount;
    }

    // 웨이팅 팀 수를 증가
    public void incrementWaitingTeamCount() {
        this.waitingTeamCount++;
    }

    // 웨이팅 팀 수를 감소
    public void decrementWaitingTeamCount() {
        if (this.waitingTeamCount > 0) {
            this.waitingTeamCount--;
        }
    }
}
