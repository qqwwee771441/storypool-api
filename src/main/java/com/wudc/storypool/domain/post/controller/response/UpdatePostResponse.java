package com.wudc.storypool.domain.post.controller.response;

import java.time.LocalDateTime;

public record UpdatePostResponse(
    String postId,
    LocalDateTime updatedAt
) {}