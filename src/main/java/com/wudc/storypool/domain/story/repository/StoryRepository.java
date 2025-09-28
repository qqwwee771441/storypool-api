package com.wudc.storypool.domain.story.repository;

import com.wudc.storypool.domain.story.entity.Story;
import com.wudc.storypool.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, String> {
    
    @Query("SELECT s FROM Story s WHERE s.userId = :userId AND s.isDeleted = false ORDER BY s.id DESC")
    List<Story> findByUserIdOrderByIdDesc(String userId, Pageable pageable);
    
    @Query("SELECT s FROM Story s WHERE s.userId = :userId AND s.isDeleted = false AND s.id < :afterCursor ORDER BY s.id DESC")
    List<Story> findByUserIdAfterCursorOrderByIdDesc(String userId, String afterCursor, Pageable pageable);
    
    Optional<Story> findByIdAndUserIdAndIsDeletedFalse(String id, String userId);

    @Query("SELECT s FROM Story s JOIN User u ON s.userId = u.id WHERE s.id = :id AND s.userId = :userId AND s.isDeleted = false")
    Optional<Story> findByIdAndUserIdWithActiveUser(String id, String userId);
}