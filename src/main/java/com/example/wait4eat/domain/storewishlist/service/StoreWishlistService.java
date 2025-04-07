package com.example.wait4eat.domain.storewishlist.service;

import com.example.wait4eat.domain.store.entity.Store;
import com.example.wait4eat.domain.store.repository.StoreRepository;
import com.example.wait4eat.domain.storewishlist.dto.response.StoreWishlistResponse;
import com.example.wait4eat.domain.storewishlist.entity.StoreWishlist;
import com.example.wait4eat.domain.storewishlist.repository.StoreWishlistRepository;
import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreWishlistService {
    private final StoreWishlistRepository storeWishlistRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void createWishlist(Long storeId, AuthUser authUser) {
        User findUser = userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new CustomException(ExceptionType.USER_NOT_FOUND)
        );
        Store findStore = storeRepository.findById(storeId).orElseThrow(() -> new CustomException(ExceptionType.STORE_NOT_FOUND));
        StoreWishlist storeWishlist = StoreWishlist.builder().user(findUser).store(findStore).build();

        if (storeWishlistRepository.existsByUserAndStore(findUser, findStore)) {
            throw new CustomException(ExceptionType.ALREADY_WISHLIST_STORE);
        }

        try {
            storeWishlistRepository.save(storeWishlist);
        } catch (CustomException ex) {
            throw new CustomException(ExceptionType.ALREADY_WISHLIST_STORE);
        }
    }

    @Transactional
    public void deleteWishlist(Long storeWishlistsId, AuthUser authUser) {
        User findUser = userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new CustomException(ExceptionType.USER_NOT_FOUND)
        );
        StoreWishlist findWishlist = storeWishlistRepository.findByIdOrElseThrow(storeWishlistsId);

        if (!findUser.getId().equals(findWishlist.getUserId())) {
            throw new CustomException(ExceptionType.NO_PERMISSION_ACTION);
        }

        storeWishlistRepository.delete(findWishlist);
    }

    public Page<StoreWishlistResponse> getAllWishlist(AuthUser authUser, int page, int size, String sort) {
        User findUser = userRepository.findById(authUser.getUserId()).orElseThrow(() -> new CustomException(ExceptionType.USER_NOT_FOUND));
        int adjustedPage = (page > 0) ? page - 1 : 0;
        if (sort == null || sort.isBlank()) {
            sort = "createdAt";
        }
        PageRequest pageable = PageRequest.of(adjustedPage, size, Sort.by(sort).descending());
        Page<StoreWishlist> wishlistPage = storeWishlistRepository.findAllByUser(findUser, pageable);

        return wishlistPage.map(storeWishlist -> StoreWishlistResponse.builder()
                .id(storeWishlist.getId())
                .store(storeWishlist.getStore())
                .createdAt(storeWishlist.getCreatedAt())
                .build());
    }
}
