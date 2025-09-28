package com.wudc.storypool.domain.story.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStoryRequest(
    @Size(min = 2, max = 100, message = "name은 2자 이상 100자 이하여야 합니다.")
    String name,

    @Size(min = 50, max = 5000, message = "text는 50자 이상 5000자 이하여야 합니다.")
    String text
) {}