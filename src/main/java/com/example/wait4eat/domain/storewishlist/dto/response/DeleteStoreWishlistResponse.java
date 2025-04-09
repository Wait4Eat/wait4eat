package com.example.wait4eat.domain.storewishlist.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class DeleteStoreWishlistResponse {
    private final Long id;

    @Builder
    private DeleteStoreWishlistResponse(Long id) {
        this.id = id;
    }
}
