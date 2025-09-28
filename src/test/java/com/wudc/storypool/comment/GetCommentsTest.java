package com.wudc.storypool.comment;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.comment.controller.response.CommentListResponse;
import com.wudc.storypool.domain.comment.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("GET /api/comments 댓글 목록 조회 테스트")
public class GetCommentsTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CommentService commentService;

    @DisplayName("댓글 목록 조회 성공")
    @Test
    public void getCommentsSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            CommentListResponse.CommentItem commentItem = new CommentListResponse.CommentItem(
                "commentId",
                "postId",
                null, // parentId (null이면 댓글)
                "Test comment",
                10L,
                5L,
                true,
                false,
                new CommentListResponse.AuthorInfo(
                    "userId", "user@test.com", "Test User", "profileUrl"
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            
            CommentListResponse mockResponse = new CommentListResponse(
                List.of(commentItem),
                true,
                "nextCursor"
            );
            
            when(commentService.getCommentsList(eq("userId"), eq("postId"), eq("latest"), isNull(), eq(20)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/comments")
                    .param("postId", "postId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments[0].id").value("commentId"))
                .andExpect(jsonPath("$.comments[0].content").value("Test comment"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("nextCursor"));
        }
    }

    @DisplayName("대댓글 목록 조회 성공")
    @Test
    public void getRepliesSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            CommentListResponse.CommentItem replyItem = new CommentListResponse.CommentItem(
                "replyId",
                "postId",
                "commentId", // parentId (대댓글이므로 부모 댓글 ID)
                "Test reply",
                0L,
                0L,
                false,
                false,
                new CommentListResponse.AuthorInfo(
                    "userId",
                    "testEmail",
                    "Test User",
                    "profileUrl"
                ),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            
            CommentListResponse mockResponse = new CommentListResponse(
                List.of(replyItem),
                false,
                null
            );
            
            when(commentService.getRepliesList(eq("userId"), eq("commentId"), isNull(), eq(20)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/comments/replies")
                    .param("commentId", "commentId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments[0].id").value("replyId"))
                .andExpect(jsonPath("$.comments[0].content").value("Test reply"))
                .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @DisplayName("정렬 옵션과 페이지네이션으로 조회")
    @Test
    public void getCommentsWithSortAndPagination() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            CommentListResponse mockResponse = new CommentListResponse(
                List.of(),
                false,
                null
            );
            
            when(commentService.getCommentsList(eq("userId"), eq("postId"), eq("popular"), eq("cursor123"), eq(10)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/comments")
                    .param("postId", "postId")
                    .param("sortBy", "popular")
                    .param("afterCursor", "cursor123")
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.hasNext").value(false));
        }
    }
}