package com.example.wait4eat.domain.waiting.entity;


import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import com.example.wait4eat.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waiting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int peopleCount;

    @Enumerated(EnumType.STRING)
    private WaitingStatus status;

    private LocalDateTime calledAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime enteredAt;

    @Builder
    public Waiting(Store store, User user, int peopleCount, WaitingStatus status) {
        this.store = store;
        this.user = user;
        this.peopleCount = peopleCount;
        this.status = status;
    }
}
