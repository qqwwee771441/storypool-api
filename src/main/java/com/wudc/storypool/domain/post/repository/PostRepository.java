package com.wudc.storypool.domain.post.repository;

import com.wudc.storypool.domain.post.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {

    // 최신순 정렬 (기본)
    @Query("SELECT p FROM Post p ORDER BY p.id DESC")
    List<Post> findAllOrderByIdDesc(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id < :afterCursor ORDER BY p.id DESC")
    List<Post> findAllAfterCursorOrderByIdDesc(@Param("afterCursor") String afterCursor, Pageable pageable);

    // 인기순 정렬 (좋아요 + 댓글 + 조회수 기준)
    @Query("SELECT p FROM Post p ORDER BY (p.likeCount + p.commentCount + p.viewCount/10) DESC, p.id DESC")
    List<Post> findAllOrderByPopularityDesc(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id < :afterCursor ORDER BY (p.likeCount + p.commentCount + p.viewCount/10) DESC, p.id DESC")
    List<Post> findAllAfterCursorOrderByPopularityDesc(@Param("afterCursor") String afterCursor, Pageable pageable);

    // 검색 기능 (제목, 내용, 태그 검색)
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t WHERE " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.id DESC")
    List<Post> findByKeywordOrderByIdDesc(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN p.tags t WHERE " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND p.id < :afterCursor " +
           "ORDER BY p.id DESC")
    List<Post> findByKeywordAfterCursorOrderByIdDesc(@Param("keyword") String keyword, @Param("afterCursor") String afterCursor, Pageable pageable);

    // 특정 사용자의 게시글 조회
    @Query("SELECT p FROM Post p WHERE p.userId = :userId ORDER BY p.id DESC")
    List<Post> findByUserIdOrderByIdDesc(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.userId = :userId AND p.id < :afterCursor ORDER BY p.id DESC")
    List<Post> findByUserIdAfterCursorOrderByIdDesc(@Param("userId") String userId, @Param("afterCursor") String afterCursor, Pageable pageable);

    // 게시글 상세 조회 (조회수 증가 없이)
    Optional<Post> findById(String id);

    // 사용자별 게시글 수 조회
    @Query("SELECT COUNT(p) FROM Post p WHERE p.userId = :userId")
    long countByUserId(@Param("userId") String userId);

    // 사용자가 좋아요한 게시글 조회 (최신순)
    @Query("SELECT p FROM Post p JOIN Like l ON p.id = l.postId WHERE l.userId = :userId ORDER BY l.id DESC")
    List<Post> findLikedPostsByUserIdOrderByLikeIdDesc(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN Like l ON p.id = l.postId WHERE l.userId = :userId AND l.id < :afterCursor ORDER BY l.id DESC")
    List<Post> findLikedPostsByUserIdAfterCursorOrderByLikeIdDesc(@Param("userId") String userId, @Param("afterCursor") String afterCursor, Pageable pageable);

    // 특정 동화와 연결된 게시글 수 조회
    @Query("SELECT COUNT(p) FROM Post p WHERE p.fairytaleId = :fairytaleId")
    long countByFairytaleId(@Param("fairytaleId") String fairytaleId);
}