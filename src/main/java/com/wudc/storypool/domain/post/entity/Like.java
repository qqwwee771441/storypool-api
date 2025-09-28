package com.wudc.storypool.domain.post.entity;

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
    name = "post_like",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_like_user_post", columnNames = {"userId", "postId"})
    },
    indexes = {
        @Index(name = "idx_post_like_post_id", columnList = "postId"),
        @Index(name = "idx_post_like_user_id", columnList = "userId")
    }
)
public class Like extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String postId;

    public static Like create(String userId, String postId) {
        Like like = new Like();
        like.userId = userId;
        like.postId = postId;
        return like;
    }
}