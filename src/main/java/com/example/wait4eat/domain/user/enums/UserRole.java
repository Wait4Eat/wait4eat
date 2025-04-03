package com.example.wait4eat.domain.user.enums;

import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    ROLE_USER(Authority.USER),
    ROLE_OWNER(Authority.OWNER);

    private final String userRole;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new CustomException(ExceptionType.INVALID_USER_ROLE));
    }

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String OWNER = "ROLE_OWNER";
    }
}