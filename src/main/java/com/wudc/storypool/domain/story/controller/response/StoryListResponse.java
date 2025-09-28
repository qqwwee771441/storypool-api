package com.wudc.storypool.domain.story.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record StoryListResponse(
    List<StoryItem> stories,
    boolean hasNext,
    String nextCursor
) {
    public record StoryItem(
        String id,
        String name,
        String excerpt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}