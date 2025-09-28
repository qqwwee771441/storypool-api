package com.wudc.storypool.domain.comment.entity;

import com.wudc.storypool.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "comment_like",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_comment_like_user_comment", columnNames = {"userId", "commentId"})
    },
    indexes = {
        @Index(name = "idx_comment_like_comment_id", columnList = "commentId"),
        @Index(name = "idx_comment_like_user_id", columnList = "userId")
    }
)
public class CommentLike extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String commentId;

    public static CommentLike create(String userId, String commentId) {
        CommentLike like = new CommentLike();
        like.userId = userId;
        like.commentId = commentId;
        return like;
    }
}