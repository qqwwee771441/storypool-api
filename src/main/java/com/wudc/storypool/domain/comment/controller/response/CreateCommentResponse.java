package com.wudc.storypool.domain.comment.controller.response;

import java.time.LocalDateTime;

public record CreateCommentResponse(
    String commentId,
    LocalDateTime createdAt
) {}