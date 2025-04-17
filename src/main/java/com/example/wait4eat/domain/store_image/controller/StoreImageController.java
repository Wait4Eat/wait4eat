package com.example.wait4eat.domain.store_image.controller;

import com.example.wait4eat.domain.store_image.dto.response.StoreImageResponse;
import com.example.wait4eat.domain.store_image.service.StoreImageService;
import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.auth.dto.AuthUser;
import com.example.wait4eat.global.dto.response.SuccessResponse;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoreImageController {

    private final StoreImageService storeImageService;

    @Secured(UserRole.Authority.OWNER)
    @PostMapping("/api/v1/stores/{storeId}/images")
    public ResponseEntity<SuccessResponse<List<StoreImageResponse>>> updateStoreImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @RequestParam("images") List<MultipartFile> images
    ) {
        if (images == null || images.isEmpty()) {
            throw new CustomException(ExceptionType.FILE_LIST_EMPTY);
        }

        return ResponseEntity.ok(
                SuccessResponse.from(storeImageService.updateStoreImages(authUser.getUserId(), storeId, images))
        );
    }

    @Secured(UserRole.Authority.OWNER)
    @DeleteMapping("/api/v1/stores/{storeId}/images/{storeImageId}")
    public ResponseEntity<SuccessResponse<Void>> deleteStoreImage(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long storeId,
            @PathVariable Long storeImageId
    ) {
        storeImageService.deleteStoreImage(authUser.getUserId(), storeId, storeImageId);
        return ResponseEntity.ok(SuccessResponse.from("이미지가 정상적으로 삭제되었습니다."));
    }

    @GetMapping("/api/v1/stores/{storeId}/images")
    public ResponseEntity<List<StoreImageResponse>> getStoreImages(
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(storeImageService.getStoreImages(storeId));
    }
}
