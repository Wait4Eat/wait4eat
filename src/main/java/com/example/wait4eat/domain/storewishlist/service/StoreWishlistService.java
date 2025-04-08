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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreWishlistService {
    private final StoreWishlistRepository storeWishlistRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public void createWishlist(Long storeId, AuthUser authUser) {
        User findUser = findUser(authUser);
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
        User findUser = findUser(authUser);
        StoreWishlist findWishlist = storeWishlistRepository.findByIdOrElseThrow(storeWishlistsId);

        if (!findUser.getId().equals(findWishlist.getUserId())) {
            throw new CustomException(ExceptionType.NO_PERMISSION_ACTION);
        }

        storeWishlistRepository.delete(findWishlist);
    }

    @Transactional(readOnly = true)
    public List<StoreWishlistResponse> getAllWishlist(AuthUser authUser, Pageable pageable) {
        User findUser = findUser(authUser);

        return storeWishlistRepository.findAllByUser(findUser, pageable)
                .stream()
                .map(StoreWishlistResponse::from)
                .toList();
    }

    private User findUser(AuthUser authUser) {
        return userRepository.findById(authUser.getUserId()).orElseThrow(
                () -> new CustomException(ExceptionType.USER_NOT_FOUND)
        );
    }
}
