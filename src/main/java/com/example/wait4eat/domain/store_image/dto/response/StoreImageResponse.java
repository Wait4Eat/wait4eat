package com.example.wait4eat.domain.store_image.dto.response;

import com.example.wait4eat.domain.store_image.entity.StoreImage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreImageResponse {

    private final Long id;
    private final String imageUrl;

    @Builder
    private StoreImageResponse(Long id, String imageUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
    }

    public static StoreImageResponse from(StoreImage storeImage) {
        return StoreImageResponse.builder()
                .id(storeImage.getId())
                .imageUrl(storeImage.getStoredFileUrl())
                .build();
    }
}
