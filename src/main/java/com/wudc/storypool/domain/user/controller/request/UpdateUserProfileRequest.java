package com.wudc.storypool.domain.user.controller.request;

import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record UpdateUserProfileRequest(
    @NotEmpty(message = "닉네임은 필수 입력 값 입니다.")
    String nickname,

    String profileImageUrl,
    String description
) {
}
