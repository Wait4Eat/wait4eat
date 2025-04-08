package com.example.wait4eat.domain.store.controller;

import com.example.wait4eat.domain.store.dto.request.CreateStoreRequest;
import com.example.wait4eat.domain.store.dto.response.CreateStoreResponse;
import com.example.wait4eat.domain.store.dto.response.GetStoreDetailResponse;
import com.example.wait4eat.domain.store.dto.response.GetStoreListResponse;
import com.example.wait4eat.domain.store.service.StoreService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/api/v1/stores")
    public ResponseEntity<List<GetStoreListResponse>> getStoreList(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(storeService.getStoreList(pageable));
    }

    @GetMapping("/api/v1/stores/{storeId}")
    public ResponseEntity<GetStoreDetailResponse> getStoreDetail(
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(storeService.getStoreDetail(storeId));
    }

}
