package com.wudc.storypool.domain.fairytale.service;

import com.wudc.storypool.common.exception.BaseException;
import com.wudc.storypool.common.exception.ErrorCode;
import com.wudc.storypool.domain.fairytale.entity.Fairytale;
import com.wudc.storypool.domain.fairytale.entity.FairytaleePage;
import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;
import com.wudc.storypool.domain.fairytale.repository.FairytalePageRepository;
import com.wudc.storypool.domain.fairytale.repository.FairytaleRepository;
import com.wudc.storypool.domain.post.repository.PostRepository;
import com.wudc.storypool.domain.story.entity.Story;
import com.wudc.storypool.domain.story.repository.StoryRepository;
import com.wudc.storypool.domain.upload.service.S3UploadService;
import com.wudc.storypool.domain.fairytale.controller.request.NotifyFairytaleCompletionRequest;
import com.wudc.storypool.global.fcm.FcmService;
import com.wudc.storypool.global.llm.LlmService;

import java.util.Arrays;
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
public class FairytaleService {

    private final FairytaleRepository fairytaleRepository;
    private final FairytalePageRepository fairytalePageRepository;
    private final PostRepository postRepository;
    private final StoryRepository storyRepository;
    private final FcmService fcmService;
    private final LlmService llmService;
    private final S3UploadService s3UploadService;

    @Transactional(readOnly = true)
    public List<Fairytale> getFairytalesList(String userId, String afterCursor, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        
        if (afterCursor == null || afterCursor.trim().isEmpty()) {
            return fairytaleRepository.findByUserIdOrderByIdDesc(userId, pageable);
        } else {
            return fairytaleRepository.findByUserIdAfterCursorOrderByIdDesc(userId, afterCursor, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Fairytale getFairytaleDetail(String userId, String fairytaleId) {
        return fairytaleRepository.findByIdAndUserId(fairytaleId, userId)
                .orElseThrow(() -> {
                    log.warn("Fairytale not found or access denied for user: {} fairytaleId: {}", userId, fairytaleId);
                    return new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND);
                });
    }

    @Transactional
    public Fairytale generateFairytale(String userId, String storyId, String name) {
        Story story = storyRepository.findByIdAndUserIdWithActiveUser(storyId, userId)
                .orElseThrow(() -> {
                    log.warn("Story not found or access denied for user: {} storyId: {}", userId, storyId);
                    return new BaseException(ErrorCode.STORY_NOT_FOUND);
                });

        List<FairytaleStatus> inProgressStatuses = Arrays.asList(
            FairytaleStatus.INIT, 
            FairytaleStatus.PENDING, 
            FairytaleStatus.PROCESSING
        );
        
        List<Fairytale> existingFairytales = fairytaleRepository.findByStoryIdAndStatusIn(storyId, inProgressStatuses);
        if (!existingFairytales.isEmpty()) {
            Fairytale existingFairytale = existingFairytales.get(0);
            log.info("Fairytale generation already in progress for storyId: {} - returning existing fairytale: {}", 
                    storyId, existingFairytale.getId());
            return existingFairytale;
        }
        
        // Create initial fairytale record
        Fairytale fairytale = Fairytale.create(userId, storyId, name);
        Fairytale savedFairytale = fairytaleRepository.save(fairytale);

        try {
            // Set status to PROCESSING
            savedFairytale.updateStatus(FairytaleStatus.PROCESSING, "동화 생성 중입니다.");
            Fairytale updatedFairytale = fairytaleRepository.save(savedFairytale);
            
            // Send request to LLM server with story text
            llmService.requestFairytaleGeneration(savedFairytale.getId(), story.getText());
            
            log.info("Fairytale generation started for user: {} storyId: {} fairytaleId: {}", 
                    userId, storyId, savedFairytale.getId());
            return updatedFairytale;
        } catch (Exception e) {
            log.error("Failed to start fairytale generation for user: {} storyId: {} fairytaleId: {}", 
                    userId, storyId, savedFairytale.getId(), e);
            
            // Update status to FAILED
            savedFairytale.updateStatus(FairytaleStatus.FAILED, "동화 생성에 실패했습니다.");
            fairytaleRepository.save(savedFairytale);
            
            throw new BaseException(ErrorCode.CANT_CONNECT_LLM);
        }
    }

    @Transactional(readOnly = true)
    public Fairytale getFairytaleStatus(String userId, String fairytaleId) {
        return fairytaleRepository.findByIdAndUserId(fairytaleId, userId)
                .orElseThrow(() -> {
                    log.warn("Fairytale not found for status check. user: {} fairytaleId: {}", userId, fairytaleId);
                    return new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND);
                });
    }

    @Transactional
    public Fairytale updateFairytale(String userId, String fairytaleId, String name) {
        Fairytale fairytale = fairytaleRepository.findByIdAndUserId(fairytaleId, userId)
                .orElseThrow(() -> {
                    log.warn("Fairytale not found or access denied for update. user: {} fairytaleId: {}", userId, fairytaleId);
                    return new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND);
                });

        fairytale.updateName(name);
        Fairytale updatedFairytale = fairytaleRepository.save(fairytale);
        
        log.info("Fairytale updated successfully for user: {} fairytaleId: {}", userId, fairytaleId);
        return updatedFairytale;
    }

    @Transactional
    public void deleteFairytale(String userId, String fairytaleId) {
        Fairytale fairytale = fairytaleRepository.findByIdAndUserId(fairytaleId, userId)
                .orElseThrow(() -> {
                    log.warn("Fairytale not found or access denied for deletion. user: {} fairytaleId: {}", userId, fairytaleId);
                    return new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND);
                });

        long postCount = postRepository.countByFairytaleId(fairytaleId);
        if (postCount > 0) {
            log.warn("Cannot delete fairytale with existing posts. fairytaleId: {} postCount: {}", fairytaleId, postCount);
            throw new BaseException(ErrorCode.FAIRYTALE_HAS_POSTS);
        }

        // Get all pages to extract image URLs for S3 deletion
        List<FairytaleePage> pages = fairytalePageRepository.findByFairytaleIdOrderByPageIndexAsc(fairytaleId);
        
        // Delete S3 images first
        for (FairytaleePage page : pages) {
            if (page.getImageUrl() != null && !page.getImageUrl().isEmpty()) {
                try {
                    String objectKey = extractObjectKeyFromUrl(page.getImageUrl());
                    if (objectKey != null) {
                        s3UploadService.deleteS3Object(objectKey);
                        log.info("Deleted S3 image for fairytale: {} page: {} objectKey: {}", fairytaleId, page.getPageIndex(), objectKey);
                    }
                } catch (Exception e) {
                    // Log error but continue with deletion process
                    log.error("Failed to delete S3 image for fairytale: {} page: {} URL: {}, error: {}", 
                            fairytaleId, page.getPageIndex(), page.getImageUrl(), e.getMessage());
                }
            }
        }

        // Delete associated pages from database
        fairytalePageRepository.deleteByFairytaleId(fairytaleId);
        
        // Delete fairytale from database
        fairytaleRepository.delete(fairytale);
        
        log.info("Fairytale deleted successfully for user: {} fairytaleId: {}", userId, fairytaleId);
    }

    @Transactional(readOnly = true)
    public boolean hasNextPage(String userId, String afterCursor, int limit) {
        List<Fairytale> fairytales = getFairytalesList(userId, afterCursor, limit + 1);
        return fairytales.size() > limit;
    }

    @Transactional
    public Fairytale generateTestFairytale(String userId) {
        // Create initial fairytale record with predefined template data
        String name = "우리들의 개발 고양이발 이야기";
        String testStoryId = "test-story-" + System.currentTimeMillis();
        Fairytale fairytale = Fairytale.create(userId, testStoryId, name);
        fairytale.setPageNumber(3);
        fairytale.updateStatus(FairytaleStatus.COMPLETED, "테스트 동화 생성 완료");
        
        Fairytale savedFairytale = fairytaleRepository.save(fairytale);

        // Upload test images from resources to S3 and get URLs
        String[] testImageFileNames = {
            "page1-developers.png",
            "page2-struggle.png",
            "page3-graduation.png"
        };

        String[] imageUrls = new String[3];
        for (int i = 0; i < testImageFileNames.length; i++) {
            try {
                imageUrls[i] = s3UploadService.uploadTestImageToS3(testImageFileNames[i]);
                log.info("Uploaded test image {} to S3 with URL: {}", testImageFileNames[i], imageUrls[i]);
            } catch (Exception e) {
                if (e instanceof BaseException) throw e;
                log.error("Failed to upload test image {}: {}", testImageFileNames[i], e.getMessage());
                // If upload fails, clean up and throw exception
                cleanupFailedTestFairytale(savedFairytale, imageUrls, i);
                throw new BaseException(ErrorCode.S3_UPLOAD_ERROR);
            }
        }

        // Create pages with predefined content
        String[] moods = {"Smile", "Sad", "Happy"};
        String[] stories = {
            "옛날 옛적에 세 명의 개발자가 있었어요.",
            "그들은 졸업을 하기 위해 고군분투 했답니다.",
            "마침내 졸업에 성공하고 행복하게 살았어요!"
        };

        for (int i = 0; i < 3; i++) {
            FairytaleePage page = FairytaleePage.create(
                savedFairytale,
                i,
                moods[i],
                stories[i],
                imageUrls[i]
            );
            fairytalePageRepository.save(page);
        }

        log.info("Test fairytale generated successfully for user: {} fairytaleId: {}", userId, savedFairytale.getId());
        return savedFairytale;
    }

    private void cleanupFailedTestFairytale(Fairytale fairytale, String[] imageUrls, int uploadedCount) {
        try {
            // Delete uploaded images from S3
            for (int i = 0; i < uploadedCount; i++) {
                if (imageUrls[i] != null) {
                    String objectKey = extractObjectKeyFromUrl(imageUrls[i]);
                    if (objectKey != null) {
                        s3UploadService.deleteS3Object(objectKey);
                    }
                }
            }
            
            // Delete fairytale and pages from database
            fairytalePageRepository.deleteByFairytaleId(fairytale.getId());
            fairytaleRepository.delete(fairytale);
            
            log.info("Cleaned up failed test fairytale creation for fairytaleId: {}", fairytale.getId());
        } catch (Exception e) {
            log.error("Failed to cleanup test fairytale after upload failure: {}", e.getMessage());
        }
    }

    private String extractObjectKeyFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }
        
        try {
            // Extract object key from S3 URL
            // URL format: https://bucket.s3.region.amazonaws.com/object-key
            String[] parts = imageUrl.split(".amazonaws.com/");
            if (parts.length == 2) {
                return parts[1];
            }
        } catch (Exception e) {
            log.warn("Failed to extract object key from URL: {}", imageUrl);
        }
        
        return null;
    }

    @Transactional
    public Fairytale updateFairytaleStatus(String fairytaleId, FairytaleStatus status, String message) {
        Fairytale fairytale = fairytaleRepository.findById(fairytaleId)
                .orElseThrow(() -> {
                    log.warn("Fairytale not found for status update. fairytaleId: {}", fairytaleId);
                    return new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND);
                });

        fairytale.updateStatus(status, message);
        Fairytale updatedFairytale = fairytaleRepository.save(fairytale);
        
        log.info("Fairytale status updated successfully. fairytaleId: {} status: {} message: {}", 
                fairytaleId, status, message);
        return updatedFairytale;
    }

    @Transactional
    public Fairytale notifyFairytaleCompletion(NotifyFairytaleCompletionRequest request) {
        // Find fairytale
        Fairytale fairytale = fairytaleRepository.findById(request.id())
                .orElseThrow(() -> {
                    log.warn("Fairytale not found for completion notification. fairytaleId: {}", request.id());
                    return new BaseException(ErrorCode.FAIRYTALE_NOT_FOUND);
                });

        // Update fairytale status and page number
        fairytale.updateStatus(request.status(), request.message());
        fairytale.setPageNumber(request.pageNumber());
        Fairytale updatedFairytale = fairytaleRepository.save(fairytale);

        // Delete existing pages (if any) and create new ones
        fairytalePageRepository.deleteByFairytaleId(request.id());
        
        for (NotifyFairytaleCompletionRequest.PageData pageData : request.pageList()) {
            FairytaleePage page = FairytaleePage.create(
                updatedFairytale,
                pageData.pageIndex(),
                pageData.mood(),
                pageData.story(),
                pageData.imageUrl()
            );
            fairytalePageRepository.save(page);
        }

        // Send FCM notification to user
        try {
            sendFairytaleCompletionNotification(updatedFairytale);
        } catch (Exception e) {
            log.error("Failed to send FCM notification for fairytale completion. fairytaleId: {} error: {}", 
                    request.id(), e.getMessage());
            // Don't fail the entire operation if notification fails
        }

        log.info("Fairytale completion processed successfully. fairytaleId: {} pages: {}", 
                request.id(), request.pageNumber());
        return updatedFairytale;
    }

    private void sendFairytaleCompletionNotification(Fairytale fairytale) {
        try {
            String userId = fairytale.getUserId();
            
            fcmService.sendFairytaleCompletionNotification(
                userId,
                fairytale.getId(),
                fairytale.getName()
            );
            
            log.info("FCM notification sent successfully for fairytale completion. fairytaleId: {} userId: {}", 
                    fairytale.getId(), userId);
        } catch (Exception e) {
            log.error("Failed to send FCM notification: {}", e.getMessage());
            throw e;
        }
    }
}