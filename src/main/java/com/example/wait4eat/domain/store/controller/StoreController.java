package com.example.wait4eat.domain.store.controller;

import com.example.wait4eat.domain.store.dto.request.CreateStoreRequest;
import com.example.wait4eat.domain.store.dto.response.CreateStoreResponse;
import com.example.wait4eat.domain.store.service.StoreService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @Secured(UserRole.Authority.OWNER)
    @PostMapping("/api/v1/stores")
    public ResponseEntity<CreateStoreResponse> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateStoreRequest request
            ) {
        return ResponseEntity.ok(storeService.create(authUser, request));
    }
}
