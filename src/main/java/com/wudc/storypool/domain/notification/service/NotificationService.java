package com.wudc.storypool.domain.notification.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.notification.controller.response.DeleteNotificationResponse;
import com.wudc.storypool.domain.notification.controller.response.MarkReadResponse;
import com.wudc.storypool.domain.notification.controller.response.NotificationListResponse;
import com.wudc.storypool.domain.notification.entity.Notification;
import com.wudc.storypool.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(String userId, int limit, String cursor, boolean unread) {
        log.info("Getting notifications for user: {}, limit: {}, cursor: {}, unread: {}", userId, limit, cursor != null ? "provided" : "null", unread);
        
        if (limit > 100) {
            log.warn("Invalid limit requested: {} for user: {}", limit, userId);
            throw new BaseException(ErrorCode.INVALID_PARAMETER);
        }
        Pageable pageable = PageRequest.of(0, limit);
        List<Notification> notifications;

        if (cursor == null || cursor.trim().isEmpty()) {
            if (unread) {
                notifications = notificationRepository.findUnreadByUserIdOrderByCreatedAtDesc(userId, pageable);
            } else {
                notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            }
        } else {
            CursorInfo cursorInfo = parseCursor(cursor);
            if (unread) {
                notifications = notificationRepository.findUnreadByUserIdAfterCursorOrderByCreatedAtDesc(
                    userId, cursorInfo.createdAt(), cursorInfo.id(), pageable);
            } else {
                notifications = notificationRepository.findByUserIdAfterCursorOrderByCreatedAtDesc(
                    userId, cursorInfo.createdAt(), cursorInfo.id(), pageable);
            }
        }

        boolean hasNext = notifications.size() == limit;
        String nextCursor = null;

        if (hasNext && !notifications.isEmpty()) {
            Notification lastNotification = notifications.get(notifications.size() - 1);
            nextCursor = createCursor(lastNotification.getCreatedAt(), lastNotification.getId());
        }

        List<NotificationListResponse.NotificationItem> notificationItems = notifications.stream()
            .map(notification -> new NotificationListResponse.NotificationItem(
                notification.getId(),
                notification.getType().name(),
                notification.getMessage(),
                notification.getTargetId(),
                notification.getIsRead(),
                notification.getCreatedAt()
            ))
            .toList();

        log.info("Successfully retrieved {} notifications for user: {}", notifications.size(), userId);
        return new NotificationListResponse(notificationItems, hasNext, nextCursor);
    }

    @Transactional
    public MarkReadResponse markNotificationsAsRead(String userId, List<String> notificationIds) {
        log.info("Marking {} notifications as read for user: {}", notificationIds != null ? notificationIds.size() : 0, userId);
        
        if (notificationIds == null || notificationIds.isEmpty()) {
            log.warn("Empty notification IDs provided for user: {}", userId);
            throw new BaseException(ErrorCode.INVALID_PARAMETER);
        }

        int updatedCount = notificationRepository.markAsReadByIdsAndUserId(notificationIds, userId);
        log.info("Successfully marked {} notifications as read for user: {}", updatedCount, userId);
        return new MarkReadResponse(updatedCount);
    }

    @Transactional
    public DeleteNotificationResponse deleteNotification(String userId, String notificationId) {
        log.info("Deleting notification: {} for user: {}", notificationId, userId);
        
        int count = notificationRepository.countByIdAndUserId(notificationId, userId);
        if (count == 0) {
            log.warn("Notification not found or access denied: {} for user: {}", notificationId, userId);
            throw new BaseException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        notificationRepository.deleteById(notificationId);
        log.info("Successfully deleted notification: {} for user: {}", notificationId, userId);
        return new DeleteNotificationResponse(true);
    }

    private CursorInfo parseCursor(String cursor) {
        String decoded = new String(Base64.getDecoder().decode(cursor));
        String[] parts = decoded.split(":");
        if (parts.length != 2) {
            log.warn("Invalid cursor format - wrong parts count: {}", cursor);
            throw new BaseException(ErrorCode.INVALID_PARAMETER);
        }
        Instant createdAt = Instant.parse(parts[0]);
        String id = parts[1];
        return new CursorInfo(createdAt, id);
    }

    private String createCursor(Instant createdAt, String id) {
        String cursorString = createdAt.toString() + ":" + id;
        return Base64.getEncoder().encodeToString(cursorString.getBytes());
    }

    private record CursorInfo(Instant createdAt, String id) {}
}