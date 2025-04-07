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

    private int myWaitingOrder;

    @Enumerated(EnumType.STRING)
    private WaitingStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime calledAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime enteredAt;

    @Builder
    public Waiting(Store store, User user, int peopleCount, int myWaitingOrder, WaitingStatus status) {
        this.store = store;
        this.user = user;
        this.peopleCount = peopleCount;
        this.myWaitingOrder = myWaitingOrder;
        this.status = status;
    }

    public void cancel(LocalDateTime cancelledAt) {
        if (this.status != WaitingStatus.CANCELLED) {
            this.status = WaitingStatus.CANCELLED;
            this.cancelledAt = cancelledAt;
        }
    }

    public void call(LocalDateTime calledAt) {
        if (this.status != WaitingStatus.CALLED) {
            this.status = WaitingStatus.CALLED;
            this.calledAt = calledAt;
        }
    }

    public void enter(LocalDateTime enteredAt) {
        if (this.status != WaitingStatus.COMPLETED) {
            this.status = WaitingStatus.COMPLETED;
            this.enteredAt = enteredAt;
        }
    }
}
