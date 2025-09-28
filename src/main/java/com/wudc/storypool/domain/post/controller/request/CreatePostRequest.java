package com.wudc.storypool.domain.post.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
    @Size(min = 2, max = 100, message = "제목은 2자 이상 100자 이하여야 합니다.")
    String title,

    @Size(min = 2, max = 1000, message = "내용은 2자 이상 1000자 이하여야 합니다.")
    String content,

    @NotBlank(message = "동화 ID는 필수입니다.")
    String fairytaleId,

    List<String> tags
) {}