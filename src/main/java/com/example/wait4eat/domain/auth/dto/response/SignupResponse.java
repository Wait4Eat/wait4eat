package com.example.wait4eat.domain.auth.dto.response;

import com.example.wait4eat.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupResponse {

    private String email;
    private String nickname;

    @Builder
    private SignupResponse(
            String email,
            String nickname
    ) {
        this.email = email;
        this.nickname = nickname;
    }

    public static SignupResponse from(User user) {
        return SignupResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }
}
