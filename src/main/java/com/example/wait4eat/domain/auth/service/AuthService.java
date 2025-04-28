package com.example.wait4eat.domain.auth.service;

import com.example.wait4eat.domain.user.entity.User;
import com.example.wait4eat.domain.user.repository.UserRepository;
import com.example.wait4eat.domain.auth.dto.request.SigninRequest;
import com.example.wait4eat.domain.auth.dto.request.SignupRequest;
import com.example.wait4eat.domain.auth.dto.response.SigninResponse;
import com.example.wait4eat.domain.auth.dto.response.SignupResponse;
import com.example.wait4eat.global.auth.jwt.JwtUtil;
import com.example.wait4eat.global.exception.CustomException;
import com.example.wait4eat.global.exception.ExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
                .role(request.getUserRole())
                .build();

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    @Transactional
    public SigninResponse signin(SigninRequest request, LocalDate today) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new CustomException(ExceptionType.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ExceptionType.INCORRECT_PASSWORD);
        }

        user.setLastLoginDate(today);
        userRepository.save(user);
        String bearerToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole());

        return SigninResponse.of(user, bearerToken);
    }
}
