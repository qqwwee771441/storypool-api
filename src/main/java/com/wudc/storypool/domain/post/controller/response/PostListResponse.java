package com.wudc.storypool.domain.post.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record PostListResponse(
    List<PostItem> posts,
    boolean hasNext,
    String nextCursor
) {
    public record PostItem(
        String id,
        String title,
        String contentPreview,
        List<String> tags,
        String thumbnailUrl,
        Long viewCount,
        Long commentCount,
        Long likeCount,
        boolean isLiked,
        boolean isMyPost,
        AuthorInfo author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}

    public record AuthorInfo(
        String id,
        String email,
        String nickName,
        String profileImageUrl
    ) {}
}