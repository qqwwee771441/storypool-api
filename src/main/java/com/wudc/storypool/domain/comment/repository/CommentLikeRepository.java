package com.wudc.storypool.domain.comment.repository;

import com.wudc.storypool.domain.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, String> {

    // 특정 사용자가 특정 댓글에 좋아요 했는지 확인
    Optional<CommentLike> findByUserIdAndCommentId(String userId, String commentId);

    // 특정 사용자가 좋아요한 댓글 ID 목록 조회 (배치 처리용)
    @Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.userId = :userId AND cl.commentId IN :commentIds")
    List<String> findLikedCommentIdsByUserIdAndCommentIds(@Param("userId") String userId, @Param("commentIds") List<String> commentIds);

    // 특정 댓글의 좋아요 수 조회
    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.commentId = :commentId")
    long countByCommentId(@Param("commentId") String commentId);

    // 특정 댓글의 모든 좋아요 삭제 (댓글 삭제시 사용)
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId = :commentId")
    void deleteByCommentId(@Param("commentId") String commentId);

    // 게시글의 모든 댓글 좋아요 삭제 (게시글 삭제시 사용)
    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId IN (SELECT c.id FROM Comment c WHERE c.postId = :postId)")
    void deleteByPostId(@Param("postId") String postId);

    // 사용자가 좋아요한 모든 댓글 ID 조회
    @Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.userId = :userId")
    List<String> findCommentIdsByUserId(@Param("userId") String userId);
}