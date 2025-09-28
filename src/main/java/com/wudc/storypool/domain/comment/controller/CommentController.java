package com.wudc.storypool.domain.comment.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.comment.controller.request.CreateCommentRequest;
import com.wudc.storypool.domain.comment.controller.request.UpdateCommentRequest;
import com.wudc.storypool.domain.comment.controller.response.CommentListResponse;
import com.wudc.storypool.domain.comment.controller.response.CreateCommentResponse;
import com.wudc.storypool.domain.comment.controller.response.UpdateCommentResponse;
import com.wudc.storypool.domain.comment.entity.Comment;
import com.wudc.storypool.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comment", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "게시글 댓글 목록 조회", description = "특정 게시글의 댓글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (postId, sortBy, limit 등)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping
    public ResponseEntity<CommentListResponse> getComments(
            @Parameter(description = "게시글 ID", required = true)
            @RequestParam String postId,
            
            @Parameter(description = "정렬 방식 (latest: 최신순, popular: 인기순)", example = "latest")
            @RequestParam(defaultValue = "latest") String sortBy,
            
            @Parameter(description = "커서 (페이지네이션용)")
            @RequestParam(required = false) String afterCursor,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        String currentUserId = AuthUtil.getUserId();
        CommentListResponse response = commentService.getCommentsList(currentUserId, postId, sortBy, afterCursor, limit);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 대댓글 목록 조회", description = "특정 댓글의 대댓글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "대댓글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (commentId, limit 등)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/replies")
    public ResponseEntity<CommentListResponse> getReplies(
            @Parameter(description = "댓글 ID", required = true)
            @RequestParam String commentId,
            
            @Parameter(description = "커서 (페이지네이션용)")
            @RequestParam(required = false) String afterCursor,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        String currentUserId = AuthUtil.getUserId();
        CommentListResponse response = commentService.getRepliesList(currentUserId, commentId, afterCursor, limit);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 생성", description = "새로운 댓글 또는 대댓글을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 생성 성공"),
        @ApiResponse(responseCode = "400", description = "요청 본문 누락 또는 검증 실패 (content 길이 등)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글 또는 부모 댓글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping
    public ResponseEntity<CreateCommentResponse> createComment(
            @Valid @RequestBody CreateCommentRequest request) {
        
        String currentUserId = AuthUtil.getUserId();
        Comment comment = commentService.createComment(
            currentUserId, 
            request.postId(), 
            request.parentId(), 
            request.content()
        );
        
        CreateCommentResponse response = new CreateCommentResponse(
            comment.getId(),
            comment.getCreatedAtByLocalDateTime()
        );
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 수정", description = "기존 댓글의 내용을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "댓글 수정 성공"),
        @ApiResponse(responseCode = "400", description = "content 검증 실패 또는 commentId 형식 오류"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "본인이 작성한 댓글이 아님"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{commentId}")
    public ResponseEntity<UpdateCommentResponse> updateComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable String commentId,
            
            @Valid @RequestBody UpdateCommentRequest request) {
        
        String currentUserId = AuthUtil.getUserId();
        Comment comment = commentService.updateComment(currentUserId, commentId, request.content());
        
        UpdateCommentResponse response = new UpdateCommentResponse(
            comment.getId(),
            comment.getUpdatedAtByLocalDateTime()
        );
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 댓글 삭제 시 대댓글도 함께 삭제됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "본인이 작성한 댓글이 아님"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable String commentId) {
        
        String currentUserId = AuthUtil.getUserId();
        commentService.deleteComment(currentUserId, commentId);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 좋아요 토글", description = "댓글에 좋아요를 추가하거나 제거합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "좋아요 토글 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Void> toggleCommentLike(
            @Parameter(description = "댓글 ID", required = true)
            @PathVariable String commentId) {
        
        String currentUserId = AuthUtil.getUserId();
        commentService.toggleLike(currentUserId, commentId);
        
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 댓글 목록 조회", description = "현재 사용자가 작성한 댓글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "내 댓글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (limit 등)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/my")
    public ResponseEntity<CommentListResponse> getMyComments(
            @Parameter(description = "커서 (페이지네이션용)")
            @RequestParam(required = false) String afterCursor,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        String currentUserId = AuthUtil.getUserId();
        CommentListResponse response = commentService.getMyCommentsList(currentUserId, afterCursor, limit);
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "좋아요한 댓글 목록 조회", description = "현재 사용자가 좋아요한 댓글 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "좋아요한 댓글 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 파라미터 (limit 등)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/liked")
    public ResponseEntity<CommentListResponse> getLikedComments(
            @Parameter(description = "커서 (페이지네이션용)")
            @RequestParam(required = false) String afterCursor,
            
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        String currentUserId = AuthUtil.getUserId();
        CommentListResponse response = commentService.getLikedCommentsList(currentUserId, afterCursor, limit);
        
        return ResponseEntity.ok(response);
    }
}