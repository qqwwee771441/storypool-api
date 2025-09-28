package com.wudc.storypool.domain.comment.controller.response;

import java.time.LocalDateTime;

public record UpdateCommentResponse(
    String commentId,
    LocalDateTime updatedAt
) {}