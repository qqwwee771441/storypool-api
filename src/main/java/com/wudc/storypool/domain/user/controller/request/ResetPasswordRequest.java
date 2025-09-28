package com.wudc.storypool.domain.user.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
    @NotBlank(message = "이메일 입력 값이 비어있습니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    String email,
    
    @NotBlank(message = "이메일 토큰 입력 값이 비어있습니다.")
    String emailToken,
    
    @NotBlank(message = "새 비밀번호 입력 값이 비어있습니다.")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~]).{8,}$",
        message = "비밀번호는 최소 8자 이상, 영문 소문자·숫자·특수문자를 모두 포함해야 합니다."
    )
    String newPassword
) {}