package com.example.wait4eat.domain.waiting.entity;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitings")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waiting{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int peopleCount;

    private int waitingTeamCount;

    private int myWaitingOrder;

    @Enumerated(EnumType.STRING)
    private WaitingStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime calledAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime enteredAt;

    @Builder
    public Waiting(Store store, User user, int peopleCount, WaitingStatus status, int waitingTeamCount, int myWaitingOrder) {
        this.store = store;
        this.user = user;
        this.peopleCount = peopleCount;
        this.waitingTeamCount = waitingTeamCount;
        this.myWaitingOrder = myWaitingOrder;
        this.status = status;
    }

    public void cancel() {
        this.status = WaitingStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }
}
