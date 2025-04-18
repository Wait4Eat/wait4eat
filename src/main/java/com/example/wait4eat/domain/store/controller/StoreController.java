package com.example.wait4eat.domain.store.controller;

import com.example.wait4eat.domain.store.dto.request.CreateStoreRequest;
import com.example.wait4eat.domain.store.dto.request.SearchStoreRequest;
import com.example.wait4eat.domain.store.dto.response.CreateStoreResponse;
import com.example.wait4eat.domain.store.dto.response.GetStoreDetailResponse;
import com.example.wait4eat.domain.store.dto.response.GetStoreListResponse;
import com.example.wait4eat.domain.store.service.StoreService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.dto.response.PageResponse;
import com.example.wait4eat.global.dto.response.SuccessResponse;
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
    public ResponseEntity<SuccessResponse<CreateStoreResponse>> create(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CreateStoreRequest request
            ) {
        return ResponseEntity.ok(SuccessResponse.from(storeService.create(authUser, request)));
    }

    @GetMapping("/api/v1/stores")
    public ResponseEntity<PageResponse<GetStoreListResponse>> getStoreList(
            @ModelAttribute SearchStoreRequest request
            ) {
        return ResponseEntity.ok(PageResponse.from(storeService.getStoreList(request)));
    }

    @GetMapping("api/v1/stores/es")
    public ResponseEntity<PageResponse<GetStoreListResponse>> searchByEs(
            @ModelAttribute SearchStoreRequest request
    ) {
        return ResponseEntity.ok(PageResponse.from(storeService.getStoreListByEs(request)));
    }

    @GetMapping("/api/v1/stores/{storeId}")
    public ResponseEntity<SuccessResponse<GetStoreDetailResponse>> getStoreDetail(
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(SuccessResponse.from(storeService.getStoreDetail(storeId)));
    }

    @GetMapping("/api/v1/stores/debug")
    public SearchStoreRequest debug(@ModelAttribute SearchStoreRequest request) {
        return request;
    }
}
