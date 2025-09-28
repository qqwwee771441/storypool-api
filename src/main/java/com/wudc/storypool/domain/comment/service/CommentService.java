package com.wudc.storypool.domain.comment.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.comment.controller.response.CommentListResponse;
import com.wudc.storypool.domain.comment.entity.Comment;
import com.wudc.storypool.domain.comment.entity.CommentLike;
import com.wudc.storypool.domain.comment.repository.CommentLikeRepository;
import com.wudc.storypool.domain.comment.repository.CommentRepository;
import com.wudc.storypool.domain.post.entity.Post;
import com.wudc.storypool.domain.post.repository.PostRepository;
import com.wudc.storypool.domain.user.entity.User;
import com.wudc.storypool.domain.user.repository.UserRepository;
import com.wudc.storypool.global.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Transactional(readOnly = true)
    public CommentListResponse getCommentsList(String currentUserId, String postId, String sortBy, String afterCursor, int limit) {
        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found for comments: {}", postId);
                    return new BaseException(ErrorCode.POST_NOT_FOUND);
                });

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Comment> comments;

        // 인기순 정렬
        if ("popular".equals(sortBy)) {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                comments = commentRepository.findCommentsByPostIdOrderByPopularityDesc(postId, pageable);
            } else {
                comments = commentRepository.findCommentsByPostIdAfterCursorOrderByPopularityDesc(postId, afterCursor, pageable);
            }
        }
        // 최신순 정렬 (기본)
        else {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                comments = commentRepository.findCommentsByPostIdOrderByIdDesc(postId, pageable);
            } else {
                comments = commentRepository.findCommentsByPostIdAfterCursorOrderByIdDesc(postId, afterCursor, pageable);
            }
        }

        return buildCommentListResponse(comments, currentUserId, limit);
    }

    @Transactional(readOnly = true)
    public CommentListResponse getRepliesList(String currentUserId, String commentId, String afterCursor, int limit) {
        // 댓글 존재 확인
        Comment parentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment not found for replies: {}", commentId);
                    return new BaseException(ErrorCode.COMMENT_NOT_FOUND);
                });

        // 댓글에 대한 대댓글만 조회 (parentId = commentId)
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Comment> replies;

        if (afterCursor == null || afterCursor.trim().isEmpty()) {
            replies = commentRepository.findRepliesByCommentIdOrderByIdDesc(commentId, pageable);
        } else {
            replies = commentRepository.findRepliesByCommentIdAfterCursorOrderByIdDesc(commentId, afterCursor, pageable);
        }

        return buildCommentListResponse(replies, currentUserId, limit);
    }

    private CommentListResponse buildCommentListResponse(List<Comment> comments, String currentUserId, int limit) {
        // 다음 페이지 존재 여부 확인
        boolean hasNext = comments.size() > limit;
        if (hasNext) {
            comments = comments.subList(0, limit);
        }
        String nextCursor = hasNext && !comments.isEmpty() ? 
                           comments.get(comments.size() - 1).getId() : null;

        // 사용자 정보와 좋아요 정보를 일괄 조회
        List<String> userIds = comments.stream().map(Comment::getUserId).distinct().collect(Collectors.toList());
        List<String> commentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());

        Map<String, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Set<String> likedCommentIds;
        if (currentUserId != null) {
            likedCommentIds = new HashSet<>(commentLikeRepository.findLikedCommentIdsByUserIdAndCommentIds(currentUserId, commentIds));
        } else {
            likedCommentIds = new HashSet<>();
        }

        // CommentItem 생성
        List<CommentListResponse.CommentItem> commentItems = comments.stream()
                .map(comment -> {
                    User author = userMap.get(comment.getUserId());
                    
                    return new CommentListResponse.CommentItem(
                        comment.getId(),
                        comment.getPostId(),
                        comment.getParentId(),
                        comment.getContent(),
                        comment.getLikeCount(),
                        comment.getReplyCount(),
                        currentUserId != null && likedCommentIds.contains(comment.getId()),
                        currentUserId != null && currentUserId.equals(comment.getUserId()),
                        author != null ? new CommentListResponse.AuthorInfo(
                            author.getId(),
                            author.getEmail(),
                            author.getNickname(),
                            author.getProfileImageUrl()
                        ) : null,
                        comment.getCreatedAtByLocalDateTime(),
                        comment.getUpdatedAtByLocalDateTime()
                    );
                })
                .collect(Collectors.toList());

        return new CommentListResponse(commentItems, hasNext, nextCursor);
    }

    @Transactional
    public Comment createComment(String userId, String postId, String parentId, String content) {
        // 게시글 존재 확인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found for comment creation: {}", postId);
                    return new BaseException(ErrorCode.POST_NOT_FOUND);
                });

        Comment comment;
        String notificationTargetUserId = null;

        if (parentId == null) {
            // 댓글 생성
            comment = Comment.createComment(userId, postId, content);
            notificationTargetUserId = post.getUserId(); // 게시글 작성자에게 알림
            
            // 게시글의 댓글 수 증가
            post.incrementCommentCount();
            postRepository.save(post);
        } else {
            // 대댓글 생성
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> {
                        log.warn("Parent comment not found: {}", parentId);
                        return new BaseException(ErrorCode.COMMENT_NOT_FOUND);
                    });

            // 대댓글은 항상 최상위 댓글에 대한 응답으로 처리 (YouTube 방식)
            String rootCommentId = parentComment.getParentId() != null ? parentComment.getParentId() : parentId;
            comment = Comment.createReply(userId, postId, rootCommentId, content);
            
            // 부모 댓글의 대댓글 수 증가
            Comment rootComment = commentRepository.findById(rootCommentId)
                    .orElseThrow(() -> new BaseException(ErrorCode.COMMENT_NOT_FOUND));
            rootComment.incrementReplyCount();
            commentRepository.save(rootComment);

            // 부모 댓글 작성자에게 알림 (본인이 아닌 경우)
            notificationTargetUserId = rootComment.getUserId();
        }

        Comment savedComment = commentRepository.save(comment);

        // FCM 푸시 알림 발송 (본인이 아닌 경우에만)
        if (notificationTargetUserId != null && !userId.equals(notificationTargetUserId)) {
            try {
                sendCommentNotification(notificationTargetUserId, post.getTitle(), comment.getParentId() == null);
            } catch (Exception e) {
                log.error("Failed to send comment notification", e);
            }
        }

        log.info("Comment created successfully: {} by user: {}", savedComment.getId(), userId);
        return savedComment;
    }

    private void sendCommentNotification(String targetUserId, String postTitle, boolean isComment) {
        // 실제 FCM 알림 로직 구현
        // fcmService.sendCommentNotification(targetUserId, postTitle, isComment);
        log.info("Comment notification sent to user: {} for post: {}", targetUserId, postTitle);
    }

    @Transactional
    public Comment updateComment(String userId, String commentId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment not found: {}", commentId);
                    return new BaseException(ErrorCode.COMMENT_NOT_FOUND);
                });

        if (!userId.equals(comment.getUserId())) {
            log.warn("Comment update access denied for user: {} commentId: {}", userId, commentId);
            throw new BaseException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        comment.updateContent(content);
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated successfully: {} by user: {}", commentId, userId);
        return updatedComment;
    }

    @Transactional
    public void deleteComment(String userId, String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment not found: {}", commentId);
                    return new BaseException(ErrorCode.COMMENT_NOT_FOUND);
                });

        if (!userId.equals(comment.getUserId())) {
            log.warn("Comment delete access denied for user: {} commentId: {}", userId, commentId);
            throw new BaseException(ErrorCode.COMMENT_ACCESS_DENIED);
        }

        // 댓글인 경우
        if (comment.isComment()) {
            // 게시글의 댓글 수 감소
            Post post = postRepository.findById(comment.getPostId())
                    .orElseThrow(() -> new BaseException(ErrorCode.POST_NOT_FOUND));
            post.decrementCommentCount();
            postRepository.save(post);

            // 대댓글들도 함께 삭제
            commentLikeRepository.deleteByCommentId(commentId);
            commentRepository.deleteByParentId(commentId);
        } else {
            // 대댓글인 경우 부모 댓글의 대댓글 수 감소
            Comment parentComment = commentRepository.findById(comment.getParentId())
                    .orElse(null);
            if (parentComment != null) {
                parentComment.decrementReplyCount();
                commentRepository.save(parentComment);
            }
        }

        // 댓글 좋아요 삭제
        commentLikeRepository.deleteByCommentId(commentId);
        
        // 댓글 삭제
        commentRepository.delete(comment);

        log.info("Comment deleted successfully: {} by user: {}", commentId, userId);
    }

    @Transactional
    public boolean toggleLike(String userId, String commentId) {
        // 댓글 존재 확인
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment not found for like toggle: {}", commentId);
                    return new BaseException(ErrorCode.COMMENT_NOT_FOUND);
                });

        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            commentLikeRepository.delete(existingLike.get());
            comment.decrementLikeCount();
            commentRepository.save(comment);
            log.info("Comment like removed: commentId={}, userId={}", commentId, userId);
            return false;
        } else {
            // 좋아요 추가
            CommentLike like = CommentLike.create(userId, commentId);
            commentLikeRepository.save(like);
            comment.incrementLikeCount();
            commentRepository.save(comment);
            log.info("Comment like added: commentId={}, userId={}", commentId, userId);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public boolean hasNextPage(String currentUserId, String postId, String sortBy, String afterCursor, int limit) {
        List<Comment> comments = getCommentsForPagination(postId, sortBy, afterCursor, limit + 1);
        return comments.size() > limit;
    }

    @Transactional(readOnly = true)
    public boolean hasNextRepliesPage(String commentId, String afterCursor, int limit) {
        List<Comment> replies = getRepliesForPagination(commentId, afterCursor, limit + 1);
        return replies.size() > limit;
    }

    private List<Comment> getCommentsForPagination(String postId, String sortBy, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        
        if ("popular".equals(sortBy)) {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                return commentRepository.findCommentsByPostIdOrderByPopularityDesc(postId, pageable);
            } else {
                return commentRepository.findCommentsByPostIdAfterCursorOrderByPopularityDesc(postId, afterCursor, pageable);
            }
        } else {
            if (afterCursor == null || afterCursor.trim().isEmpty()) {
                return commentRepository.findCommentsByPostIdOrderByIdDesc(postId, pageable);
            } else {
                return commentRepository.findCommentsByPostIdAfterCursorOrderByIdDesc(postId, afterCursor, pageable);
            }
        }
    }

    @Transactional(readOnly = true)
    public CommentListResponse getMyCommentsList(String userId, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Comment> comments;

        if (afterCursor == null || afterCursor.trim().isEmpty()) {
            comments = commentRepository.findByUserIdOrderByIdDesc(userId, pageable);
        } else {
            comments = commentRepository.findByUserIdAfterCursorOrderByIdDesc(userId, afterCursor, pageable);
        }

        return buildCommentListResponse(comments, userId, limit);
    }

    @Transactional(readOnly = true)
    public CommentListResponse getLikedCommentsList(String userId, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Comment> comments;

        if (afterCursor == null || afterCursor.trim().isEmpty()) {
            comments = commentRepository.findLikedCommentsByUserIdOrderByLikeIdDesc(userId, pageable);
        } else {
            comments = commentRepository.findLikedCommentsByUserIdAfterCursorOrderByLikeIdDesc(userId, afterCursor, pageable);
        }

        return buildCommentListResponse(comments, userId, limit);
    }

    private List<Comment> getRepliesForPagination(String commentId, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        
        if (afterCursor == null || afterCursor.trim().isEmpty()) {
            return commentRepository.findRepliesByCommentIdOrderByIdDesc(commentId, pageable);
        } else {
            return commentRepository.findRepliesByCommentIdAfterCursorOrderByIdDesc(commentId, afterCursor, pageable);
        }
    }
}