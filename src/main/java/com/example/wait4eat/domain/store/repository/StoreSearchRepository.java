package com.example.wait4eat.domain.store.repository;

import com.example.wait4eat.domain.store.dto.request.SearchStoreRequest;
import com.example.wait4eat.domain.store.entity.StoreDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "feature.search.enabled", havingValue = "true")
public interface StoreSearchRepository extends ElasticsearchRepository<StoreDocument, String>, StoreSearchRepositoryCustom {

    Page<StoreDocument> searchStores(SearchStoreRequest request, Pageable pageable);
}

