package com.wudc.storypool.domain.user.controller.request;

import jakarta.validation.constraints.NotEmpty;

public record UpdatePasswordRequest(
    @NotEmpty(message = "새로운 비밀번호가 입력되지 않았습니다.")
    String newPassword,
    @NotEmpty(message = "기존 비밀번호가 입력되지 않았습니다.")
    String password
) {
}
