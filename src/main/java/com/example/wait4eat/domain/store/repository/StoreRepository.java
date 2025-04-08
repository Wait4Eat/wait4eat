package com.example.wait4eat.domain.store.repository;

import com.example.wait4eat.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByUserId (Long userId);

    Page<Store> findAll(Pageable pageable);
}
