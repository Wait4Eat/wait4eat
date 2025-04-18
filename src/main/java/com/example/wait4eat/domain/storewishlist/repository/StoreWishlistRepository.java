package com.example.wait4eat.domain.storewishlist.repository;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.storewishlist.entity.StoreWishlist;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StoreWishlistRepository extends JpaRepository<StoreWishlist, Long> {
    default StoreWishlist findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new CustomException(ExceptionType.STORE_WISHLIST_NOT_FOUND));
    }
    boolean existsByUserAndStore(User user, Store store);

    Page<StoreWishlist> findAllByUser(User user, Pageable pageable);

    @Query("SELECT sw.user FROM StoreWishlist sw WHERE sw.store.id = :storeId")
    List<User> findAllUsersByStoreId(Long storeId);
}
