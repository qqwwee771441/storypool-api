package com.wudc.storypool.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.comment.controller.request.CreateCommentRequest;
import com.wudc.storypool.domain.comment.entity.Comment;
import com.wudc.storypool.domain.comment.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /api/comments 댓글 생성 테스트")
public class CreateCommentTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CommentService commentService;

    @DisplayName("댓글 생성 성공")
    @Test
    public void createCommentSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Comment mockComment = Comment.builder()
                .content("Test comment")
                .build();

            mockComment.setId("commentId");
            mockComment.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
            
            when(commentService.createComment("userId", "postId", null, "Test comment"))
                .thenReturn(mockComment);

            // when
            CreateCommentRequest request = new CreateCommentRequest(
                "postId",
                null,
                "Test comment"
            );

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/comments")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value("commentId"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }
    }

    @DisplayName("대댓글 생성 성공")
    @Test
    public void createReplySuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Comment mockComment = Comment.builder()
                .content("Test reply")
                .build();

            mockComment.setId("replyId");
            mockComment.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
            
            when(commentService.createComment("userId", "postId", "parentCommentId", "Test reply"))
                .thenReturn(mockComment);

            // when
            CreateCommentRequest request = new CreateCommentRequest(
                "postId",
                "parentCommentId",
                "Test reply"
            );

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/comments")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value("replyId"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }
    }

    @DisplayName("내용이 빈 문자열로 요청")
    @Test
    public void createCommentWithEmptyContent() throws Exception {
        // when
        CreateCommentRequest request = new CreateCommentRequest(
            "postId",
            null,
            ""
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/comments")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("댓글 내용은 2자 이상 1000자 이하여야 합니다."));
    }

    @DisplayName("게시글 ID가 빈 문자열로 요청")
    @Test
    public void createCommentWithEmptyPostId() throws Exception {
        // when
        CreateCommentRequest request = new CreateCommentRequest(
            "",
            null,
            "Test comment"
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/comments")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("게시글 ID는 필수입니다."));
    }

    @DisplayName("내용이 너무 긴 경우")
    @Test
    public void createCommentWithLongContent() throws Exception {
        // when - 1001자 내용 (1000자 초과)
        String longContent = "a".repeat(1001);
        CreateCommentRequest request = new CreateCommentRequest(
            "postId",
            null,
            longContent
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/comments")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("댓글 내용은 2자 이상 1000자 이하여야 합니다."));
    }
}