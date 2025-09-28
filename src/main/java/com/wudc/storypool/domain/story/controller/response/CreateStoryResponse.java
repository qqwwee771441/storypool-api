package com.wudc.storypool.domain.story.controller.response;

import java.time.LocalDateTime;

public record CreateStoryResponse(
    String draftId,
    LocalDateTime createdAt
) {}