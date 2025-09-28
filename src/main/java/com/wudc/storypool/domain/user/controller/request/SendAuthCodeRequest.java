package com.wudc.storypool.domain.user.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record SendAuthCodeRequest(
    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotEmpty(message = "이메일 입력 값이 비어있습니다.")
    String email
) {
}
