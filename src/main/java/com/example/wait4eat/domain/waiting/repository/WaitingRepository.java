package com.example.wait4eat.domain.waiting.repository;

import com.example.wait4eat.domain.waiting.entity.Waiting;
import com.example.wait4eat.domain.waiting.enums.WaitingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long>, WaitingQueryRepository {
    List<Waiting> findByStoreIdAndStatusOrderByCreatedAtAsc(Long storeId, WaitingStatus status);
}
