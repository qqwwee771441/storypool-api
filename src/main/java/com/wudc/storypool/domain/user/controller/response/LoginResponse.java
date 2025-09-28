package com.wudc.storypool.domain.user.controller.response;

public record LoginResponse(
    String accessToken,
    String refreshToken
) {
}