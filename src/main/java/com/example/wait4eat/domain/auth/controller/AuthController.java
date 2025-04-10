package com.example.wait4eat.domain.auth.controller;

import com.example.wait4eat.domain.auth.dto.request.SigninRequest;
import com.example.wait4eat.domain.auth.dto.request.SignupRequest;
import com.example.wait4eat.domain.auth.dto.response.SigninResponse;
import com.example.wait4eat.domain.auth.dto.response.SignupResponse;
import com.example.wait4eat.domain.auth.service.AuthService;
import com.example.wait4eat.global.dto.response.SuccessResponse;
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
    public ResponseEntity<SuccessResponse<SignupResponse>> signup(
            @Valid @RequestBody SignupRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.from(authService.signup(request)));
    }

    @PostMapping("/api/v1/auth/signin")
    public ResponseEntity<SuccessResponse<SigninResponse>> signin(
            @Valid @RequestBody SigninRequest request
    ) {
        return ResponseEntity.ok(SuccessResponse.from(authService.signin(request)));
    }
}
