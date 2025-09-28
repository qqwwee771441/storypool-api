package com.wudc.storypool.domain.fairytale.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFairytaleRequest(
    @NotBlank(message = "name은 필수입니다.")
    @Size(min = 2, max = 100, message = "name은 2자 이상 100자 이하여야 합니다.")
    String name
) {}