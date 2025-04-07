package com.example.wait4eat.domain.storewishlist.controller;

import com.example.wait4eat.domain.storewishlist.service.StoreWishlistService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StoreWishlistController {
    private final StoreWishlistService storeWishlistService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/api/v1/storewishlist/{storeId}")
    public ResponseEntity<String> createWishlist(@PathVariable Long storeId, @AuthenticationPrincipal AuthUser authUser) {
        storeWishlistService.createWishlist(storeId,authUser);
        return ResponseEntity.ok("가게를 찜하였습니다");
    }
}
