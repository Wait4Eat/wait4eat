package com.example.wait4eat.domain.store.repository;

import com.example.wait4eat.domain.store.dto.request.SearchStoreRequest;
import com.example.wait4eat.domain.store.entity.StoreDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Repository;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.stream.Collectors;

@Repository
public class StoreSearchRepositoryImpl implements StoreSearchRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;

    public StoreSearchRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Page<StoreDocument> searchStores(SearchStoreRequest request, Pageable pageable) {
        Criteria criteria = new Criteria();

        if (request.getName() != null && !request.getName().isEmpty()) {
            criteria.and(new Criteria("name").matches(request.getName()));
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            criteria.and(new Criteria("address").matches(request.getAddress()));
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            criteria.and(new Criteria("description").matches(request.getDescription()));
        }
        if (request.getOpenTime() != null) {
            criteria.and(new Criteria("openTime").greaterThanEqual(request.getOpenTime()));
        }
        if (request.getCloseTime() != null) {
            criteria.and(new Criteria("closeTime").lessThanEqual(request.getCloseTime()));
        }

        Query query = new CriteriaQuery(criteria)
                .setPageable(pageable)
                .addSort(Sort.by(Sort.Order.desc("_score"))); // 점수 내림차순 정렬 추가

        SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);
        return new PageImpl<>(
                searchHits.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .collect(Collectors.toList()),
                pageable,
                searchHits.getTotalHits()
        );
    }
}