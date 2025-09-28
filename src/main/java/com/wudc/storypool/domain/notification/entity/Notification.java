package com.wudc.storypool.domain.notification.entity;

import com.wudc.storypool.common.base.BaseEntity;
import com.wudc.storypool.domain.notification.entity.constant.NotificationType;
import com.wudc.storypool.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
    @Index(name = "idx_notification_user_created_id", columnList = "user_id, createdAt, id"),
    @Index(name = "idx_notification_user_read", columnList = "user_id, isRead")
})
public class Notification extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false, length = 1000, columnDefinition = "VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String message;
    
    @Column(length = 26)
    private String targetId;
    
    @Column(nullable = false)
    @ColumnDefault(value = "false")
    private Boolean isRead = false;
    
    public static Notification create(User user, NotificationType type, String message, String targetId) {
        Notification notification = new Notification();
        notification.user = user;
        notification.type = type;
        notification.message = message;
        notification.targetId = targetId;
        notification.isRead = false;
        return notification;
    }
    
    public void markAsRead() {
        this.isRead = true;
    }
}