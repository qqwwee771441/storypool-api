package com.wudc.storypool.domain.story.entity;

import com.wudc.storypool.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Story extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 5000)
    private String text;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean isDeleted = false;

    public static Story create(String userId, String name, String text) {
        return Story.builder()
                .userId(userId)
                .name(name)
                .text(text)
                .isDeleted(false)
                .build();
    }

    public void updateContent(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }

    public String getExcerpt() {
        if (text == null) {
            return "";
        }
        return text.length() > 100 ? text.substring(0, 100) + "…" : text;
    }
}