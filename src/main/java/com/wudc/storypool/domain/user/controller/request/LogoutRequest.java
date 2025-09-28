package com.wudc.storypool.domain.user.controller.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "액세스 토큰 입력 값이 비어있습니다.")
    String accessToken,
    
    @NotBlank(message = "리프레시 토큰 입력 값이 비어있습니다.")
    String refreshToken
) {}