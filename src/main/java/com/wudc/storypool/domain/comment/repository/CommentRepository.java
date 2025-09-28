package com.wudc.storypool.domain.comment.repository;

import com.wudc.storypool.domain.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    // 게시글의 댓글 목록 조회 (최신순)
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentId IS NULL ORDER BY c.id DESC")
    List<Comment> findCommentsByPostIdOrderByIdDesc(@Param("postId") String postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentId IS NULL AND c.id < :afterCursor ORDER BY c.id DESC")
    List<Comment> findCommentsByPostIdAfterCursorOrderByIdDesc(@Param("postId") String postId, @Param("afterCursor") String afterCursor, Pageable pageable);

    // 게시글의 댓글 목록 조회 (인기순)
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentId IS NULL ORDER BY (c.likeCount + c.replyCount) DESC, c.id DESC")
    List<Comment> findCommentsByPostIdOrderByPopularityDesc(@Param("postId") String postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.parentId IS NULL AND c.id < :afterCursor ORDER BY (c.likeCount + c.replyCount) DESC, c.id DESC")
    List<Comment> findCommentsByPostIdAfterCursorOrderByPopularityDesc(@Param("postId") String postId, @Param("afterCursor") String afterCursor, Pageable pageable);

    // 댓글의 대댓글 목록 조회 (최신순)
    @Query("SELECT c FROM Comment c WHERE c.parentId = :commentId ORDER BY c.id DESC")
    List<Comment> findRepliesByCommentIdOrderByIdDesc(@Param("commentId") String commentId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.parentId = :commentId AND c.id < :afterCursor ORDER BY c.id DESC")
    List<Comment> findRepliesByCommentIdAfterCursorOrderByIdDesc(@Param("commentId") String commentId, @Param("afterCursor") String afterCursor, Pageable pageable);

    // 특정 댓글 조회
    Optional<Comment> findById(String id);

    // 게시글의 댓글 수 조회
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.parentId IS NULL")
    long countCommentsByPostId(@Param("postId") String postId);

    // 댓글의 대댓글 수 조회
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentId = :commentId")
    long countRepliesByCommentId(@Param("commentId") String commentId);

    // 게시글의 모든 댓글 삭제 (게시글 삭제시 사용)
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.postId = :postId")
    void deleteByPostId(@Param("postId") String postId);

    // 댓글의 모든 대댓글 삭제 (댓글 삭제시 사용)
    @Modifying
    @Query("DELETE FROM Comment c WHERE c.parentId = :commentId")
    void deleteByParentId(@Param("commentId") String commentId);

    // 사용자의 모든 댓글 조회
    @Query("SELECT c FROM Comment c WHERE c.userId = :userId ORDER BY c.id DESC")
    List<Comment> findByUserIdOrderByIdDesc(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.userId = :userId AND c.id < :afterCursor ORDER BY c.id DESC")
    List<Comment> findByUserIdAfterCursorOrderByIdDesc(@Param("userId") String userId, @Param("afterCursor") String afterCursor, Pageable pageable);

    // 사용자가 좋아요한 댓글 조회 (최신순)
    @Query("SELECT c FROM Comment c JOIN CommentLike cl ON c.id = cl.commentId WHERE cl.userId = :userId ORDER BY cl.id DESC")
    List<Comment> findLikedCommentsByUserIdOrderByLikeIdDesc(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN CommentLike cl ON c.id = cl.commentId WHERE cl.userId = :userId AND cl.id < :afterCursor ORDER BY cl.id DESC")
    List<Comment> findLikedCommentsByUserIdAfterCursorOrderByLikeIdDesc(@Param("userId") String userId, @Param("afterCursor") String afterCursor, Pageable pageable);

    // 특정 게시글에 해당 사용자가 작성한 댓글 수
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.postId = :postId AND c.userId = :userId")
    long countByPostIdAndUserId(@Param("postId") String postId, @Param("userId") String userId);
}