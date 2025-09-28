package com.wudc.storypool.domain.user.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WithdrawalRequest(
    @Size(min = 1, max = 100, message = "비밀번호는 1자 이상 100자 이하여야 합니다.")
    String password,
    
    @NotBlank(message = "액세스 토큰 입력 값이 비어있습니다.")
    String accessToken,
    
    @NotBlank(message = "리프레시 토큰 입력 값이 비어있습니다.")
    String refreshToken
) {}