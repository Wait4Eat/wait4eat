package com.example.wait4eat.domain.store.repository;

import com.example.wait4eat.domain.store.dto.request.SearchStoreRequest;
import com.example.wait4eat.domain.store.entity.StoreDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreSearchRepositoryCustom {
    Page<StoreDocument> searchStores(SearchStoreRequest request, Pageable pageable);
}