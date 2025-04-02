package com.example.wait4eat.domain.store_image.entity;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String storedFileName;

    private String imageUrl;

    @Builder
    public StoreImage(Store store, User user, String storedFileName, String imageUrl) {
        this.store = store;
        this.user = user;
        this.storedFileName = storedFileName;
        this.imageUrl = imageUrl;
    }
}
