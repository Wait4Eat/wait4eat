package com.example.wait4eat.domain.storewishlist.controller;

import com.example.wait4eat.domain.storewishlist.dto.response.StoreWishlistResponse;
import com.example.wait4eat.domain.storewishlist.service.StoreWishlistService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<String> createWishlist(@PathVariable Long storeId, @AuthenticationPrincipal AuthUser authUser) {
        storeWishlistService.createWishlist(storeId,authUser);
        return ResponseEntity.ok("가게를 찜하였습니다");
    }

    @Secured(UserRole.Authority.USER)
    @DeleteMapping("/api/v1/storewishlists/{storeWishlistsId}")
    public ResponseEntity<String> deleteWishlist(@PathVariable Long storeWishlistsId, @AuthenticationPrincipal AuthUser authUser) {
        storeWishlistService.deleteWishlist(storeWishlistsId, authUser);
        return ResponseEntity.ok("찜하기를 취소 하였습니다.");
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/api/v1/storewishlists")
    public ResponseEntity<Page<StoreWishlistResponse>> getWishlist(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", required = false) String sort
    ) {
        Page<StoreWishlistResponse> wishlist = storeWishlistService.getAllWishlist(authUser, page, size, sort);
        return ResponseEntity.ok(wishlist);
    }
}
