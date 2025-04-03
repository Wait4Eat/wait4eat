package com.example.wait4eat.global.auth.controller;

import com.example.wait4eat.global.auth.dto.request.SignupRequest;
import com.example.wait4eat.global.auth.dto.response.SignupResponse;
import com.example.wait4eat.global.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/auth/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        return ResponseEntity.ok(authService.signup(request));
    }
}
