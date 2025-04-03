package com.example.wait4eat.global.auth.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SigninRequest {

    private String email;
    private String password;

    @Builder
    public SigninRequest(
            String email,
            String password
    ) {
        this.email = email;
        this.password = password;
    }
}
