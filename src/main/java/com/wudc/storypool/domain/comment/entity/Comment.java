package com.wudc.storypool.domain.comment.entity;

import com.wudc.storypool.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(indexes = {
    @Index(name = "idx_comment_post_id", columnList = "postId"),
    @Index(name = "idx_comment_parent_id", columnList = "parentId"),
    @Index(name = "idx_comment_user_id", columnList = "userId"),
    @Index(name = "idx_comment_post_parent", columnList = "postId, parentId")
})
public class Comment extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String postId;

    // null이면 댓글, 값이 있으면 대댓글
    @Column(nullable = true)
    private String parentId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false)
    private Long replyCount = 0L;

    public static Comment createComment(String userId, String postId, String content) {
        return Comment.builder()
                .userId(userId)
                .postId(postId)
                .parentId(null) // 댓글은 parentId가 null
                .content(content)
                .likeCount(0L)
                .replyCount(0L)
                .build();
    }

    public static Comment createReply(String userId, String postId, String parentId, String content) {
        return Comment.builder()
                .userId(userId)
                .postId(postId)
                .parentId(parentId)
                .content(content)
                .likeCount(0L)
                .replyCount(0L)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementReplyCount() {
        this.replyCount++;
    }

    public void decrementReplyCount() {
        if (this.replyCount > 0) {
            this.replyCount--;
        }
    }

    public boolean isComment() {
        return parentId == null;
    }

    public boolean isReply() {
        return parentId != null;
    }
}