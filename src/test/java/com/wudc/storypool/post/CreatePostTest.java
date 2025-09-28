package com.wudc.storypool.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.post.controller.request.CreatePostRequest;
import com.wudc.storypool.domain.post.entity.Post;
import com.wudc.storypool.domain.post.service.PostService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /api/posts 게시글 생성 테스트")
public class CreatePostTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PostService postService;

    @DisplayName("성공")
    @Test
    public void createPostSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Post mockPost = Post.builder()
                .title("Test Title")
                .content("Test Content")
                .build();

            mockPost.setId("postId");
            mockPost.setCreatedAt(Instant.now());
            when(postService.createPost(
                eq("userId"),
                eq("Test Title"),
                eq("Test Content"),
                eq("fairytaleId"),
                eq(List.of("tag1", "tag2"))
            )).thenReturn(mockPost);

            // when
            CreatePostRequest request = new CreatePostRequest(
                "Test Title",
                "Test Content",
                "fairytaleId",
                List.of("tag1", "tag2")
            );

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/posts")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId").value("postId"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }
    }

    @DisplayName("제목이 빈 문자열로 요청")
    @Test
    public void createPostWithEmptyTitle() throws Exception {
        // when
        CreatePostRequest request = new CreatePostRequest(
            "",
            "Test Content",
            "fairytaleId",
            List.of("tag1", "tag2")
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/posts")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("제목은 2자 이상 100자 이하여야 합니다."));
    }

    @DisplayName("내용이 빈 문자열로 요청")
    @Test
    public void createPostWithEmptyContent() throws Exception {
        // when
        CreatePostRequest request = new CreatePostRequest(
            "Test Title",
            "",
            "fairytaleId",
            List.of("tag1", "tag2")
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/posts")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("내용은 2자 이상 1000자 이하여야 합니다."));
    }

    @DisplayName("제목이 너무 긴 경우")
    @Test
    public void createPostWithLongTitle() throws Exception {
        // when - 201자 제목 (200자 초과)
        String longTitle = "a".repeat(201);
        CreatePostRequest request = new CreatePostRequest(
            longTitle,
            "Test Content",
            "fairytaleId",
            List.of("tag1", "tag2")
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/posts")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("제목은 2자 이상 100자 이하여야 합니다."));
    }
}