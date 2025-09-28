package com.wudc.storypool.domain.story.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.story.entity.Story;
import com.wudc.storypool.domain.story.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryService {

    private final StoryRepository storyRepository;

    @Transactional
    public Story createStory(String userId, String name, String text) {
        Story story = Story.create(userId, name, text);
        Story savedStory = storyRepository.save(story);
        
        log.info("Story created successfully for user: {} with id: {}", userId, savedStory.getId());
        return savedStory;
    }

    @Transactional(readOnly = true)
    public List<Story> getStoriesList(String userId, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit + 1);
        
        if (afterCursor == null || afterCursor.trim().isEmpty()) {
            return storyRepository.findByUserIdOrderByIdDesc(userId, pageable);
        } else {
            return storyRepository.findByUserIdAfterCursorOrderByIdDesc(userId, afterCursor, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Story getStoryDetail(String userId, String storyId) {
        return storyRepository.findByIdAndUserIdAndIsDeletedFalse(storyId, userId)
                .orElseThrow(() -> {
                    log.warn("Story not found or access denied for user: {} storyId: {}", userId, storyId);
                    return new BaseException(ErrorCode.STORY_NOT_FOUND);
                });
    }

    @Transactional
    public Story updateStory(String userId, String storyId, String name, String text) {
        Story story = storyRepository.findByIdAndUserIdAndIsDeletedFalse(storyId, userId)
                .orElseThrow(() -> {
                    log.warn("Story not found or access denied for update. user: {} storyId: {}", userId, storyId);
                    return new BaseException(ErrorCode.STORY_NOT_FOUND);
                });

        story.updateContent(name, text);
        
        log.info("Story updated successfully for user: {} storyId: {}", userId, storyId);
        return story;
    }

    @Transactional
    public void deleteStory(String userId, String storyId) {
        Story story = storyRepository.findByIdAndUserIdAndIsDeletedFalse(storyId, userId)
                .orElseThrow(() -> {
                    log.warn("Story not found or access denied for deletion. user: {} storyId: {}", userId, storyId);
                    return new BaseException(ErrorCode.STORY_NOT_FOUND);
                });

        storyRepository.delete(story);
        log.info("Story deleted successfully for user: {} storyId: {}", userId, storyId);
    }

}