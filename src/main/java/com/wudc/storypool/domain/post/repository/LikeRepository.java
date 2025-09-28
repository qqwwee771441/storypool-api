package com.wudc.storypool.domain.post.repository;

import com.wudc.storypool.domain.post.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, String> {

    // 특정 사용자가 특정 게시글에 좋아요 했는지 확인
    Optional<Like> findByUserIdAndPostId(String userId, String postId);

    // 특정 사용자가 좋아요한 게시글 ID 목록 조회 (배치 처리용)
    @Query("SELECT l.postId FROM Like l WHERE l.userId = :userId AND l.postId IN :postIds")
    List<String> findLikedPostIdsByUserIdAndPostIds(@Param("userId") String userId, @Param("postIds") List<String> postIds);

    // 특정 게시글의 좋아요 수 조회
    @Query("SELECT COUNT(l) FROM Like l WHERE l.postId = :postId")
    long countByPostId(@Param("postId") String postId);

    // 특정 게시글의 모든 좋아요 삭제 (게시글 삭제시 사용)
    @Modifying
    @Query("DELETE FROM Like l WHERE l.postId = :postId")
    void deleteByPostId(@Param("postId") String postId);

    // 사용자가 좋아요한 모든 게시글 ID 조회
    @Query("SELECT l.postId FROM Like l WHERE l.userId = :userId")
    List<String> findPostIdsByUserId(@Param("userId") String userId);
}