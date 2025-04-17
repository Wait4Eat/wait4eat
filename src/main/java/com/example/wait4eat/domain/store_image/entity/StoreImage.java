package com.example.wait4eat.domain.store_image.entity;

import com.example.wait4eat.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_images")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String storedFileUrl;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public StoreImage(Store store, String storedFileName, String storedFileUrl) {
        this.store = store;
        this.storedFileName = storedFileName;
        this.storedFileUrl = storedFileUrl;
    }
}
