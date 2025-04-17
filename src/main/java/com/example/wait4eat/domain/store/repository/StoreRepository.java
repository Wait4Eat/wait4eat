package com.example.wait4eat.domain.store.repository;

import com.example.wait4eat.domain.store.entity.Store;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

import java.time.LocalDate;

public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByUserId (Long userId);

    Page<Store> findAll(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Store s where s.id = :storeId")
    Optional<Store> findByIdWithPessimisticLock(Long storeId);

    Long countByCreatedAt(LocalDate yesterday);
}
