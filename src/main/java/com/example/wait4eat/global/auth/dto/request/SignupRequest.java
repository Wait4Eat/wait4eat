package com.example.wait4eat.global.auth.dto.request;

import com.example.wait4eat.domain.user.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupRequest {

    private String email;
    private String password;
    private String nickname;
    private UserRole role;

    @Builder
    public SignupRequest(
            String email,
            String password,
            String nickname,
            UserRole role
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
    }

}
