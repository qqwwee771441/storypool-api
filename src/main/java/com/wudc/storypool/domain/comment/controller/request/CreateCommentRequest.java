package com.wudc.storypool.domain.comment.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
    @NotBlank(message = "게시글 ID는 필수입니다.")
    String postId,

    String parentId, // null이면 댓글, 값이 있으면 대댓글

    @Size(min = 2, max = 1000, message = "댓글 내용은 2자 이상 1000자 이하여야 합니다.")
    String content
) {}