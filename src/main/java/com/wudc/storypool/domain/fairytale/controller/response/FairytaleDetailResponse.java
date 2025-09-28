package com.wudc.storypool.domain.fairytale.controller.response;

import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;

import java.time.LocalDateTime;
import java.util.List;

public record FairytaleDetailResponse(
    String id,
    String name,
    int pageNumber,
    List<PageInfo> pageList,
    FairytaleStatus status,
    String message,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record PageInfo(
        int pageIndex,
        String mood,
        String story,
        String imageUrl
    ) {}
}