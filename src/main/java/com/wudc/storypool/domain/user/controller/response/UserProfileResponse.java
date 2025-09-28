package com.wudc.storypool.domain.user.controller.response;

public record UserProfileResponse(
    String email,
    String nickname,
    String profileImageUrl,
    String description
) {
}
