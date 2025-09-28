package com.wudc.storypool.domain.fairytale.controller.response;

import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FairytaleListResponse(
    List<FairytaleItem> fairytales,
    boolean hasNext,
    String nextCursor
) {
    public record FairytaleItem(
        String id,
        String name,
        int pageNumber,
        ThumbnailInfo thumbnail,
        FairytaleStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
    
    public record ThumbnailInfo(
        int pageIndex,
        String mood,
        String story,
        String imageUrl
    ) {}
}