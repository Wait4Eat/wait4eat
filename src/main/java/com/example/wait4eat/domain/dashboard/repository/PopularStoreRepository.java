package com.example.wait4eat.domain.dashboard.repository;

import com.example.wait4eat.domain.dashboard.entity.PopularStore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularStoreRepository extends JpaRepository<PopularStore, Long> {
}
