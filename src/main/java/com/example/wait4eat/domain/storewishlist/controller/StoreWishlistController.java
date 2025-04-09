package com.example.wait4eat.domain.storewishlist.controller;

import com.example.wait4eat.domain.storewishlist.dto.response.DeleteStoreWishlistResponse;
import com.example.wait4eat.domain.storewishlist.dto.response.StoreWishlistResponse;
import com.example.wait4eat.domain.storewishlist.service.StoreWishlistService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.dto.response.PageResponse;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StoreWishlistController {
    private final StoreWishlistService storeWishlistService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/api/v1/storewishlists/{storeId}")
    public ResponseEntity<SuccessResponse<StoreWishlistResponse>> createWishlist(@PathVariable Long storeId, @AuthenticationPrincipal AuthUser authUser) {
        StoreWishlistResponse response = storeWishlistService.createWishlist(storeId, authUser);
        return ResponseEntity.ok(SuccessResponse.from(response));
    }

    @Secured(UserRole.Authority.USER)
    @DeleteMapping("/api/v1/storewishlists/{storeWishlistsId}")
    public ResponseEntity<SuccessResponse<DeleteStoreWishlistResponse>> deleteWishlist(@PathVariable Long storeWishlistsId, @AuthenticationPrincipal AuthUser authUser) {
        storeWishlistService.deleteWishlist(storeWishlistsId, authUser);
        return ResponseEntity.ok(SuccessResponse.from("요청이 성공적으로 처리되었습니다."));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/api/v1/storewishlists")
    public ResponseEntity<PageResponse<StoreWishlistResponse>> getWishlist(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<StoreWishlistResponse> wishlist = storeWishlistService.getAllWishlist(authUser, pageable);
        return ResponseEntity.ok(PageResponse.from(wishlist));
    }
}
