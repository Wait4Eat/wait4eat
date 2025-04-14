package com.example.wait4eat.domain.store.service;

import com.example.wait4eat.domain.store.dto.request.CreateStoreRequest;
import com.example.wait4eat.domain.store.dto.request.SearchStoreRequest;
import com.example.wait4eat.domain.store.dto.response.CreateStoreResponse;
import com.example.wait4eat.domain.store.dto.response.GetStoreDetailResponse;
import com.example.wait4eat.domain.store.dto.response.GetStoreListResponse;
import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateStoreResponse create(AuthUser authUser, CreateStoreRequest request) {

        User user = userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new CustomException(ExceptionType.USER_NOT_FOUND)
        );

        if (storeRepository.existsByUserId(user.getId())) {
            throw new CustomException(ExceptionType.STORE_ALREADY_EXISTS);
        }

        Store store = Store.builder()
                .user(user)
                .name(request.getName())
                .address(request.getAddress())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .description(request.getDescription())
                .depositAmount(request.getDepositAmount())
                .build();

        Store savedStore = storeRepository.save(store);

        return CreateStoreResponse.of(savedStore, user);
    }

    @Transactional(readOnly = true)
    public Page<GetStoreListResponse> getStoreList(SearchStoreRequest request) {
        System.out.println("SearchStoreRequest: name=" + request.getName() +
                ", address=" + request.getAddress() +
                ", page=" + request.getPage() +
                ", size=" + request.getSize());

        // 정렬 설정
        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("asc") ?
                        Sort.Order.asc(request.getSort()) :
                        Sort.Order.desc(request.getSort())
        );

        // 페이징 설정
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        // 검색 및 필터링 조건 적용
        return storeRepository.searchStores(request, pageable)
                .map(GetStoreListResponse::from);
    }

    @Transactional(readOnly = true)
    public GetStoreDetailResponse getStoreDetail(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new CustomException(ExceptionType.STORE_NOT_FOUND)
        );

        return GetStoreDetailResponse.of(store, store.getUser());
    }
}
