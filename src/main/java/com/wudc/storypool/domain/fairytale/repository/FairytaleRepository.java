package com.wudc.storypool.domain.fairytale.repository;

import com.wudc.storypool.domain.fairytale.entity.Fairytale;
import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FairytaleRepository extends JpaRepository<Fairytale, String> {
    
    @Query("SELECT f FROM Fairytale f WHERE f.userId = :userId ORDER BY f.id DESC")
    List<Fairytale> findByUserIdOrderByIdDesc(String userId, Pageable pageable);
    
    @Query("SELECT f FROM Fairytale f WHERE f.userId = :userId AND f.id < :afterCursor ORDER BY f.id DESC")
    List<Fairytale> findByUserIdAfterCursorOrderByIdDesc(String userId, String afterCursor, Pageable pageable);
    
    @EntityGraph(attributePaths = {"pageList"})
    Optional<Fairytale> findByIdAndUserId(String id, String userId);
    
    boolean existsByIdAndUserId(String id, String userId);
    
    @Query("SELECT f FROM Fairytale f WHERE f.storyId = :storyId AND f.status IN :statuses")
    List<Fairytale> findByStoryIdAndStatusIn(String storyId, List<FairytaleStatus> statuses);
    
    Optional<Fairytale> findByUserIdAndStoryId(String userId, String storyId);
}