package com.example.wait4eat.global.auth.service;

import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.global.auth.dto.request.SignupRequest;
import com.example.wait4eat.global.auth.dto.response.SignupResponse;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ExceptionType.USER_ALREADY_EXISTS);
        }

        String encodePassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .password(encodePassword)
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        return new SignupResponse(savedUser.getEmail(), savedUser.getNickname());
    }
}
