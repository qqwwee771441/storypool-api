package com.wudc.storypool.domain.story.controller.response;

public record StoryDetailResponse(
    String id,
    String name,
    String text
) {}