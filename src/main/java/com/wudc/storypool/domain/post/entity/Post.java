package com.wudc.storypool.domain.post.entity;

import com.wudc.storypool.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private String fairytaleId;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false)
    private Long commentCount = 0L;

    public static Post create(String userId, String title, String content, String fairytaleId, List<String> tags) {
        return Post.builder()
                .userId(userId)
                .title(title)
                .content(content)
                .fairytaleId(fairytaleId)
                .tags(tags != null ? new ArrayList<>(tags) : new ArrayList<>())
                .viewCount(0L)
                .likeCount(0L)
                .commentCount(0L)
                .build();
    }

    public void updateContent(String title, String content, String fairytaleId, List<String> tags) {
        this.title = title;
        this.content = content;
        this.fairytaleId = fairytaleId;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public String getContentPreview() {
        if (content == null) {
            return "";
        }
        return content.length() > 100 ? content.substring(0, 100) + "…" : content;
    }
}