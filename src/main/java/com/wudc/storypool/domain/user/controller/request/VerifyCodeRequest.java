package com.wudc.storypool.domain.user.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(
    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotEmpty(message = "이메일 입력 값이 비어있습니다.")
    String email,
    
    @NotEmpty(message = "인증 코드 입력 값이 비어있습니다.")
    @Pattern(regexp = "^\\d{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
    String code
) {
}