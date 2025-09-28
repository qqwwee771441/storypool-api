package com.wudc.storypool.domain.story.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.story.controller.request.CreateStoryRequest;
import com.wudc.storypool.domain.story.controller.request.UpdateStoryRequest;
import com.wudc.storypool.domain.story.controller.response.*;
import com.wudc.storypool.domain.story.entity.Story;
import com.wudc.storypool.domain.story.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Story", description = "스토리 초안 관리 API")
@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    @Operation(summary = "스토리 초안 목록 조회", description = "사용자의 스토리 초안 목록을 커서 기반 페이징으로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "limit 값이 범위 벗어남"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping
    public StoryListResponse getStories(
        @RequestParam(required = false) String after,
        @RequestParam(defaultValue = "20") int limit
    ) {
        if (limit > 50) limit = 50;
        if (limit < 1) limit = 1;

        String userId = AuthUtil.getUserId();
        List<Story> stories = storyService.getStoriesList(userId, after, limit);
        
        // Check if there are more stories
        boolean hasNext = stories.size() > limit;
        if (hasNext) {
            stories = stories.subList(0, limit);
        }
        
        // Get next cursor (last story id if has next)
        String nextCursor = hasNext && !stories.isEmpty() ? 
                           stories.get(stories.size() - 1).getId() : null;

        List<StoryListResponse.StoryItem> storyItems = stories.stream()
                .map(story -> new StoryListResponse.StoryItem(
                    story.getId(),
                    story.getName(),
                    story.getExcerpt(),
                    story.getCreatedAtByLocalDateTime(),
                    story.getUpdatedAtByLocalDateTime()
                ))
                .toList();

        return new StoryListResponse(storyItems, hasNext, nextCursor);
    }

    @Operation(summary = "스토리 초안 상세 조회", description = "특정 스토리 초안의 전체 내용을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "다른 사용자의 초안 조회 시도"),
        @ApiResponse(responseCode = "404", description = "스토리 초안을 찾을 수 없습니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/{id}")
    public StoryDetailResponse getStoryDetail(@PathVariable String id) {
        String userId = AuthUtil.getUserId();
        Story story = storyService.getStoryDetail(userId, id);
        
        return new StoryDetailResponse(
            story.getId(),
            story.getName(),
            story.getText()
        );
    }

    @Operation(summary = "스토리 초안 생성", description = "새로운 스토리 초안을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "초안 생성 성공"),
        @ApiResponse(responseCode = "400", description = "text 필드 누락 또는 빈 문자열, text 길이 초과"),
        @ApiResponse(responseCode = "401", description = "JWT 토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateStoryResponse createStory(@RequestBody @Valid CreateStoryRequest request) {
        String userId = AuthUtil.getUserId();
        Story story = storyService.createStory(userId, request.name(), request.text());
        
        return new CreateStoryResponse(story.getId(), story.getCreatedAtByLocalDateTime());
    }

    @Operation(summary = "스토리 초안 수정", description = "기존 스토리 초안의 내용을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "초안 수정 성공"),
        @ApiResponse(responseCode = "400", description = "text 누락 또는 길이 범위 위반, draftId 형식 오류"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음, 대상 초안이 본인 소유가 아님"),
        @ApiResponse(responseCode = "404", description = "해당 draftId에 대응하는 초안이 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{id}")
    public UpdateStoryResponse updateStory(
        @PathVariable String id,
        @RequestBody @Valid UpdateStoryRequest request
    ) {
        String userId = AuthUtil.getUserId();
        Story story = storyService.updateStory(userId, id, request.name(), request.text());
        
        return new UpdateStoryResponse(story.getId(), story.getUpdatedAtByLocalDateTime());
    }

    @Operation(summary = "스토리 초안 삭제", description = "특정 스토리 초안을 삭제합니다. 삭제된 초안은 복구할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "초안 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 혹은 유효하지 않음, 초안이 본인 소유가 아님"),
        @ApiResponse(responseCode = "404", description = "해당 draftId에 대응하는 초안이 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{id}")
    public Map<String, String> deleteStory(@PathVariable String id) {
        String userId = AuthUtil.getUserId();
        storyService.deleteStory(userId, id);
        
        return Map.of("message", "Draft deleted successfully.");
    }
}