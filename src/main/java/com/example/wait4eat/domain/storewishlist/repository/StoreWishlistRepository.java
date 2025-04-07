package com.example.wait4eat.domain.storewishlist.repository;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.storewishlist.entity.StoreWishlist;
import com.example.wait4eat.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreWishlistRepository extends JpaRepository<StoreWishlist, Long> {
    boolean existsByUserAndStore(User user, Store store);
}
