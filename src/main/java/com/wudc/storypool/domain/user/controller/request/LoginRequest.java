package com.wudc.storypool.domain.user.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotEmpty(message = "이메일 입력 값이 비어있습니다.")
    String email,
    
    @NotEmpty(message = "비밀번호 입력 값이 비어있습니다.")
    @Size(min = 1, max = 100, message = "비밀번호는 1자 이상 100자 이하여야 합니다.")
    String password
) {
}