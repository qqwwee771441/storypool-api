package com.wudc.storypool.domain.post.controller.response;

import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
    String id,
    String title,
    String content,
    List<String> tags,
    Long viewCount,
    Long commentCount,
    Long likeCount,
    boolean isLiked,
    boolean isMyPost,
    AuthorInfo author,
    FairytaleInfo fairytale,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record AuthorInfo(
        String id,
        String email,
        String nickName,
        String profileImageUrl
    ) {}

    public record FairytaleInfo(
        String id,
        String name,
        Integer pageNumber,
        List<PageInfo> pageList,
        FairytaleStatus status,
        String message,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record PageInfo(
        Integer pageIndex,
        String mood,
        String story,
        String imageUrl
    ) {}
}