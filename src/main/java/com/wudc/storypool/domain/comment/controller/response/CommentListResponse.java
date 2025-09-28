package com.wudc.storypool.domain.comment.controller.response;

import java.time.LocalDateTime;
import java.util.List;

public record CommentListResponse(
    List<CommentItem> comments,
    boolean hasNext,
    String nextCursor
) {
    public record CommentItem(
        String id,
        String postId,
        String parentId, // null이면 댓글, 값이 있으면 대댓글
        String content,
        Long likeCount,
        Long replyCount,
        boolean isLiked,
        boolean isMyComment,
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