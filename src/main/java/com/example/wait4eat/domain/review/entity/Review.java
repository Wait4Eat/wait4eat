package com.example.wait4eat.domain.review.entity;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waiting_id", nullable = false)
    private Waiting waiting;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private double rating;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Builder
    private Review(Waiting waiting, double rating, String content) {
        this.waiting = waiting;
        this.rating = rating;
        this.content = content;
    }

    public Long getUserId() {
        return (waiting != null) ? waiting.getUser().getId() : null;
    }

    public Long getStoreId() {
        return (waiting != null) ? waiting.getStore().getId() : null;
    }
}
