package com.wudc.storypool.domain.notification.entity;

import com.wudc.storypool.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSettings extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean pushEnabled = true;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean emailEnabled = false;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean onComment = true;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean onReply = true;

    @Column(nullable = false)
    @ColumnDefault("false")
    private Boolean onLike = false;

    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean onFairytaleComplete = true;

    public static NotificationSettings createDefault(String userId) {
        NotificationSettings settings = new NotificationSettings();
        settings.userId = userId;
        settings.pushEnabled = true;
        settings.emailEnabled = false;
        settings.onComment = true;
        settings.onReply = true;
        settings.onLike = false;
        settings.onFairytaleComplete = true;
        return settings;
    }

    public void updateSettings(boolean pushEnabled, boolean emailEnabled, boolean onComment, 
                              boolean onReply, boolean onLike, boolean onFairytaleComplete) {
        this.pushEnabled = pushEnabled;
        this.emailEnabled = emailEnabled;
        this.onComment = onComment;
        this.onReply = onReply;
        this.onLike = onLike;
        this.onFairytaleComplete = onFairytaleComplete;
    }
}