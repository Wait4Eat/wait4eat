package com.example.wait4eat.domain.storewishlist.repository;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.storewishlist.entity.StoreWishlist;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreWishlistRepository extends JpaRepository<StoreWishlist, Long> {
    default StoreWishlist findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new CustomException(ExceptionType.STORE_WISHLIST_NOT_FOUND));
    }
    boolean existsByUserAndStore(User user, Store store);
}
