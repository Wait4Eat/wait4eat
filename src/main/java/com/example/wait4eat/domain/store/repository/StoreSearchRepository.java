package com.example.wait4eat.domain.store.repository;

import com.example.wait4eat.domain.store.entity.StoreDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalTime;

public interface StoreSearchRepository extends ElasticsearchRepository<StoreDocument, String> {

    // 기본 검색: name.nori, address.nori, description.nori 필드 대상
    Page<StoreDocument> findByNameContainingOrAddressContainingOrDescriptionContaining(
            String name, String address, String description, Pageable pageable
    );

    // 시간 필터링 포함 검색
    Page<StoreDocument> findByNameContainingOrAddressContainingOrDescriptionContainingAndOpenTimeGreaterThanEqualAndCloseTimeLessThanEqual(
            String name, String address, String description,
            LocalTime openTime, LocalTime closeTime, Pageable pageable
    );
}

