package com.example.wait4eat.domain.store.repository;

import com.example.wait4eat.domain.store.dto.request.SearchStoreRequest;
import com.example.wait4eat.domain.store.entity.Store;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StoreRepository extends JpaRepository<Store, Long> {
    boolean existsByUserId (Long userId);

    Page<Store> findAll(Pageable pageable);

    @Query("SELECT s FROM Store s " +
            "WHERE (:name IS NULL OR s.name LIKE %:name%) " +
            "AND (:address IS NULL OR s.address LIKE %:address%) " +
            "AND (:description IS NULL OR s.description LIKE %:description%) " +
            "AND (" +
            "    (:openTime IS NULL AND :closeTime IS NULL) OR " +
            "    (" +
            "        s.openTime < s.closeTime AND " +
            "        (:openTime IS NULL OR s.openTime <= :openTime) AND " +
            "        (:closeTime IS NULL OR s.closeTime >= :closeTime)" +
            "    ) OR (" +
            "        s.openTime > s.closeTime AND " +
            "        (" +
            "            (:openTime IS NULL OR s.openTime <= :openTime) OR " +
            "            (:closeTime IS NULL OR s.closeTime >= :closeTime)" +
            "        )" +
            "    )" +
            ")")
    Page<Store> searchStores(
            @Param("name") String name,
            @Param("address") String address,
            @Param("description") String description,
            @Param("openTime") LocalTime openTime,
            @Param("closeTime") LocalTime closeTime,
            Pageable pageable
    );

    // SearchStoreRequest를 직접 처리하도록 오버로드
    default Page<Store> searchStores(SearchStoreRequest request, Pageable pageable) {
        return searchStores(
                request.getName(),
                request.getAddress(),
                request.getDescription(),
                request.getOpenTime(),
                request.getCloseTime(),
                pageable
        );
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Store s where s.id = :storeId")
    Optional<Store> findByIdWithPessimisticLock(Long storeId);

    Long countByCreatedAt(LocalDate yesterday);

    @Query("""
    SELECT s
    FROM Store s
    JOIN Waiting w ON w.store = s
    WHERE w.createdAt BETWEEN :startOfDay AND :endOfDay
    GROUP BY s.id
    ORDER BY COUNT(w.id) DESC
    """)
    List<Store> findTop10StoresByWaitingCount(@Param("startOfDay") LocalDateTime start, @Param("endOfDay") LocalDateTime end, Pageable pageable);

    Long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
