package com.wudc.storypool.domain.user.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record TestSignupRequest(
    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotEmpty(message = "이메일 입력 값이 비어있습니다.")
    String email,
    
    @NotEmpty(message = "비밀번호 입력 값이 비어있습니다.")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "비밀번호는 8자 이상, 영문·숫자·특수문자 조합이어야 합니다."
    )
    String password,
    
    @NotEmpty(message = "닉네임은 필수입니다.")
    String nickname,
    
    String description
) {}