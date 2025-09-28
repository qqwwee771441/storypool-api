package com.wudc.storypool.domain.user.controller.response;

public record RefreshTokenResponse(
    String accessToken,
    String refreshToken
) {}