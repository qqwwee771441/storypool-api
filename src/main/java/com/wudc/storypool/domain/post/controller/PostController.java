package com.wudc.storypool.domain.post.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.post.controller.request.CreatePostRequest;
import com.wudc.storypool.domain.post.controller.request.UpdatePostRequest;
import com.wudc.storypool.domain.post.controller.response.*;
import com.wudc.storypool.domain.post.entity.Post;
import com.wudc.storypool.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시글 관련 API")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 정렬 및 키워드 검색 조건으로 조회합니다. 비로그인 사용자도 접근 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (sortBy, limit 등)"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public PostListResponse getPosts(
        @RequestParam(defaultValue = "latest") String sortBy, // latest, popular
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String after,
        @RequestParam(defaultValue = "20") int limit
    ) {
        if (limit > 50) limit = 50;
        if (limit < 1) limit = 1;

        // 인증이 선택적이므로 현재 사용자 ID를 안전하게 가져옴
        String currentUserId = null;
        try {
            currentUserId = AuthUtil.getUserId();
        } catch (Exception e) {
            // 비로그인 사용자인 경우 null로 처리
        }
        
        return postService.getPostsList(currentUserId, sortBy, keyword, after, limit);
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다. 비로그인 사용자도 접근 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 상세 조회 성공"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/{id}")
    public PostDetailResponse getPostDetail(@PathVariable String id) {
        // 인증이 선택적이므로 현재 사용자 ID를 안전하게 가져옴
        String currentUserId = null;
        try {
            currentUserId = AuthUtil.getUserId();
        } catch (Exception e) {
            // 비로그인 사용자인 경우 null로 처리
        }
        
        return postService.getPostDetail(currentUserId, id);
    }

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
        @ApiResponse(responseCode = "400", description = "요청 본문 누락 또는 검증 실패 (title, content 등)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 동화 ID (참조한 경우)"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatePostResponse createPost(@RequestBody @Valid CreatePostRequest request) {
        String userId = AuthUtil.getUserId();
        Post post = postService.createPost(
            userId,
            request.title(),
            request.content(),
            request.fairytaleId(),
            request.tags()
        );
        
        return new CreatePostResponse(
            post.getId(),
            post.getCreatedAtByLocalDateTime()
        );
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글의 내용을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
        @ApiResponse(responseCode = "400", description = "요청 본문 검증 실패 또는 id 형식 오류"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "본인이 작성한 게시글이 아님"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{id}")
    public UpdatePostResponse updatePost(
        @PathVariable String id,
        @RequestBody @Valid UpdatePostRequest request
    ) {
        String userId = AuthUtil.getUserId();
        Post post = postService.updatePost(
            userId,
            id,
            request.title(),
            request.content(),
            request.fairytaleId(),
            request.tags()
        );
        
        return new UpdatePostResponse(
            post.getId(),
            post.getUpdatedAtByLocalDateTime()
        );
    }

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "본인이 작성한 게시글이 아님"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{id}")
    public Map<String, String> deletePost(@PathVariable String id) {
        String userId = AuthUtil.getUserId();
        postService.deletePost(userId, id);
        
        return Map.of("message", "Post deleted successfully.");
    }

    @Operation(summary = "게시글 좋아요 토글", description = "게시글에 좋아요를 추가하거나 제거합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "좋아요 토글 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/{id}/like")
    public Map<String, Object> toggleLike(@PathVariable String id) {
        String userId = AuthUtil.getUserId();
        boolean isLiked = postService.toggleLike(userId, id);
        
        return Map.of(
            "isLiked", isLiked,
            "message", isLiked ? "좋아요가 추가되었습니다." : "좋아요가 취소되었습니다."
        );
    }

    @Operation(summary = "내 게시글 목록 조회", description = "현재 사용자가 작성한 게시글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 게시글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (limit 등)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/my")
    public PostListResponse getMyPosts(
            @Parameter(description = "커서 (페이지네이션용)")
            @RequestParam(required = false) String afterCursor,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        if (limit > 50) limit = 50;
        if (limit < 1) limit = 1;
        
        String userId = AuthUtil.getUserId();
        return postService.getMyPostsList(userId, afterCursor, limit);
    }

    @Operation(summary = "좋아요한 게시글 목록 조회", description = "현재 사용자가 좋아요한 게시글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "좋아요한 게시글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (limit 등)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/liked")
    public PostListResponse getLikedPosts(
            @Parameter(description = "커서 (페이지네이션용)")
            @RequestParam(required = false) String afterCursor,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        if (limit > 50) limit = 50;
        if (limit < 1) limit = 1;
        
        String userId = AuthUtil.getUserId();
        return postService.getLikedPostsList(userId, afterCursor, limit);
    }
}