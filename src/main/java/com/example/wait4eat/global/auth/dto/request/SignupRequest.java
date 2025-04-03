package com.example.wait4eat.global.auth.dto.request;

import com.example.wait4eat.domain.user.enums.UserRole;
import com.example.wait4eat.global.consts.Const;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupRequest {

    @Email
    @NotBlank(message = "이메일 입력은 필수입니다.")
    private String email;

    @NotBlank(message = "비밃번호 입력은 필수입니다.")
    @Size(min = 8)
    @Pattern(
            regexp = Const.PASSWORD_PATTERN,
            message = "비밀번호 형식이 올바르지 않습니다."
    )
    private String password;

    private String nickname;

    @NotNull(message = "유저 롤 입력은 필수입니다.")
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
