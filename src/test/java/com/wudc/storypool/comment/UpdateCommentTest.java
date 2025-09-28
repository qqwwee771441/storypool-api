package com.wudc.storypool.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.comment.controller.request.UpdateCommentRequest;
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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("PATCH /api/comments/{commentId} 댓글 수정 테스트")
public class UpdateCommentTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CommentService commentService;

    @DisplayName("댓글 수정 성공")
    @Test
    public void updateCommentSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Comment mockComment = Comment.builder()
                .content("Updated comment")
                .build();
            mockComment.setId("commentId");
            mockComment.setUpdatedAt(Instant.parse("2025-01-01T00:00:00Z"));
            
            when(commentService.updateComment("userId", "commentId", "Updated comment"))
                .thenReturn(mockComment);

            // when
            UpdateCommentRequest request = new UpdateCommentRequest("Updated comment");

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/comments/commentId")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value("commentId"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
        }
    }

    @DisplayName("내용이 빈 문자열로 요청")
    @Test
    public void updateCommentWithEmptyContent() throws Exception {
        // when
        UpdateCommentRequest request = new UpdateCommentRequest("");

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/comments/commentId")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("댓글 내용은 2자 이상 1000자 이하여야 합니다."));
    }
}