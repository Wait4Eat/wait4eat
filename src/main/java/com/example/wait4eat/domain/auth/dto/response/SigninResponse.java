package com.example.wait4eat.domain.auth.dto.response;

import com.example.wait4eat.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SigninResponse {

    private Long id;
    private String bearerToken;

    @Builder
    private SigninResponse(
            Long id,
            String bearerToken
    ) {
        this.id = id;
        this.bearerToken = bearerToken;
    }

    public static SigninResponse of(User user, String bearerToken) {
        return SigninResponse.builder()
                .id(user.getId())
                .bearerToken(bearerToken)
                .build();
    }
}
