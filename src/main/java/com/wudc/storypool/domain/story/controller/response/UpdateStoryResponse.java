package com.wudc.storypool.domain.story.controller.response;

import java.time.LocalDateTime;

public record UpdateStoryResponse(
    String draftId,
    LocalDateTime updatedAt
) {}