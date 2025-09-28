package com.wudc.storypool.domain.notification.repository;

import com.wudc.storypool.domain.notification.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC, n.id DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId " +
           "AND (n.createdAt < :cursorCreatedAt OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId)) " +
           "ORDER BY n.createdAt DESC, n.id DESC")
    List<Notification> findByUserIdAfterCursorOrderByCreatedAtDesc(
            @Param("userId") String userId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") String cursorId,
            Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC, n.id DESC")
    List<Notification> findUnreadByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false " +
           "AND (n.createdAt < :cursorCreatedAt OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId)) " +
           "ORDER BY n.createdAt DESC, n.id DESC")
    List<Notification> findUnreadByUserIdAfterCursorOrderByCreatedAtDesc(
            @Param("userId") String userId,
            @Param("cursorCreatedAt") Instant cursorCreatedAt,
            @Param("cursorId") String cursorId,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :notificationIds AND n.user.id = :userId")
    int markAsReadByIdsAndUserId(@Param("notificationIds") List<String> notificationIds, @Param("userId") String userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.id = :notificationId AND n.user.id = :userId")
    int countByIdAndUserId(@Param("notificationId") String notificationId, @Param("userId") String userId);
}