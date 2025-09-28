package com.wudc.storypool.global.security.jwt.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}