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

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class StoreSearchRepositoryImpl implements StoreSearchRepositoryCustom {

    private final ElasticsearchOperations elasticsearchOperations;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    public StoreSearchRepositoryImpl(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Page<StoreDocument> searchStores(SearchStoreRequest request, Pageable pageable) {
        Criteria criteria = new Criteria();

        // 기본 검색 조건
        if (request.getName() != null && !request.getName().isEmpty()) {
            criteria.and(new Criteria("name").matches(request.getName()));
        }
        if (request.getAddress() != null && !request.getAddress().isEmpty()) {
            criteria.and(new Criteria("address").matches(request.getAddress()));
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            criteria.and(new Criteria("description").matches(request.getDescription()));
        }

        // 시간 조건
        if (request.getOpenTime() != null && request.getCloseTime() != null) {
            String requestOpen = request.getOpenTime().format(formatter);
            String requestClose = request.getCloseTime().format(formatter);

            // openTime <= 요청 openTime
            criteria.and(new Criteria("openTime").lessThanEqual(requestOpen));
            // closeTime >= 요청 closeTime
            criteria.and(new Criteria("closeTime").greaterThanEqual(requestClose));

            // 자정 넘김(overnight) 케이스도 포함해야 함
            // closeTime < openTime 인 경우(예: 18:00~02:00), closeTime에 24시간을 더해 비교
            // Elasticsearch에 저장된 시간은 문자열이므로, 실제 비교는 애플리케이션에서 필터링 필요

        } else if (request.getOpenTime() != null) {
            String requestOpen = request.getOpenTime().format(formatter);
            criteria.and(new Criteria("openTime").lessThanEqual(requestOpen));
        } else if (request.getCloseTime() != null) {
            String requestClose = request.getCloseTime().format(formatter);
            criteria.and(new Criteria("closeTime").greaterThanEqual(requestClose));
        }

        Query query = new CriteriaQuery(criteria)
                .setPageable(pageable)
                .addSort(Sort.by(Sort.Order.desc("_score")));

        SearchHits<StoreDocument> searchHits = elasticsearchOperations.search(query, StoreDocument.class);
        List<StoreDocument> results = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        // overnight 케이스(자정 넘김) 추가 필터링
        if (request.getOpenTime() != null && request.getCloseTime() != null) {
            int reqOpen = request.getOpenTime().getHour() * 60 + request.getOpenTime().getMinute();
            int reqClose = request.getCloseTime().getHour() * 60 + request.getCloseTime().getMinute();

            results = results.stream().filter(doc -> {
                int storeOpen = Integer.parseInt(doc.getOpenTime().replace(":", "")) / 100 * 60 +
                        Integer.parseInt(doc.getOpenTime().replace(":", "")) % 100;
                int storeClose = Integer.parseInt(doc.getCloseTime().replace(":", "")) / 100 * 60 +
                        Integer.parseInt(doc.getCloseTime().replace(":", "")) % 100;

                if (storeClose > storeOpen) {
                    // 일반 케이스
                    return storeOpen <= reqOpen && storeClose >= reqClose;
                } else {
                    // overnight 케이스 (예: 18:00~02:00)
                    // closeTime에 24*60 더해서 비교
                    int storeCloseOvernight = storeClose + 24 * 60;
                    int reqCloseOvernight = reqClose < storeOpen ? reqClose + 24 * 60 : reqClose;
                    return storeOpen <= reqOpen && storeCloseOvernight >= reqCloseOvernight;
                }
            }).collect(Collectors.toList());
        }

        // 중복 제거
        Set<String> seenIds = new HashSet<>();
        List<StoreDocument> uniqueResults = results.stream()
                .filter(doc -> seenIds.add(String.valueOf(doc.getId())))
                .collect(Collectors.toList());

        long totalHits = uniqueResults.size();
        return new PageImpl<>(uniqueResults, pageable, totalHits);
    }

}