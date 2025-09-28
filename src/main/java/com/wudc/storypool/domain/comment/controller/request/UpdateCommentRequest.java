package com.wudc.storypool.domain.comment.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommentRequest(
    @Size(min = 2, max = 1000, message = "댓글 내용은 2자 이상 1000자 이하여야 합니다.")
    String content
) {}