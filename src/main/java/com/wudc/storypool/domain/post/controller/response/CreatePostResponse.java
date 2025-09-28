package com.wudc.storypool.domain.post.controller.response;

import java.time.LocalDateTime;

public record CreatePostResponse(
    String postId,
    LocalDateTime createdAt
) {}