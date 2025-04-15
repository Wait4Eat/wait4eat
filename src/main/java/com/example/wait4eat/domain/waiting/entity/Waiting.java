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

    @Column(unique = true) // 주문 ID는 유일해야 함
    private String orderId;

    @Column(nullable = false)
    private int peopleCount;

    private int myWaitingOrder; // 확정된 내 웨이팅 순서 (결제 후)

    @Enumerated(EnumType.STRING)
    private WaitingStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime activatedAt;

    private LocalDateTime calledAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime enteredAt;

    @Builder
    public Waiting(Store store, User user, String orderId, int peopleCount, int myWaitingOrder, WaitingStatus status) {
        this.store = store;
        this.user = user;
        this.orderId = orderId;
        this.peopleCount = peopleCount;
        this.myWaitingOrder = myWaitingOrder;
        this.status = status;
    }

    public void waiting(LocalDateTime activatedAt) {
        if (this.status != WaitingStatus.WAITING) {
            this.status = WaitingStatus.WAITING;
            this.activatedAt = activatedAt;
        }
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

    public void markAsCalled() {
        this.myWaitingOrder = 0;
    }

    public void updateMyWaitingOrder(int newOrder) {
        this.myWaitingOrder = newOrder;
    }

    public void incrementMyWaitingOrder() {
        this.myWaitingOrder++;
    }

}
