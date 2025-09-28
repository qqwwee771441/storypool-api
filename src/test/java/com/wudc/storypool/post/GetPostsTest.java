package com.wudc.storypool.post;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.post.controller.response.PostListResponse;
import com.wudc.storypool.domain.post.service.PostService;
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
@DisplayName("GET /api/posts 게시글 목록 조회 테스트")
public class GetPostsTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PostService postService;

    @DisplayName("로그인 상태에서 게시글 목록 조회 성공")
    @Test
    public void getPostsSuccessWithAuth() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            PostListResponse.PostItem postItem = new PostListResponse.PostItem(
                "postId",
                "Test Title",
                "Test Content",
                List.of("tag1", "tag2"),
                "thumbnailUrl",
                100L,
                5L,
                10L,
                true,
                false,
                new PostListResponse.AuthorInfo("authorId", "author@test.com", "authorName", "authorProfileImageUrl"),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            
            PostListResponse mockResponse = new PostListResponse(
                List.of(postItem),
                true,
                "nextCursor"
            );
            
            when(postService.getPostsList(eq("userId"), eq("latest"), isNull(), isNull(), eq(20)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts[0].id").value("postId"))
                .andExpect(jsonPath("$.posts[0].title").value("Test Title"))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("nextCursor"));
        }
    }

    @DisplayName("비로그인 상태에서 게시글 목록 조회 성공")
    @Test
    public void getPostsSuccessWithoutAuth() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenThrow(new RuntimeException("Not authenticated"));
            
            PostListResponse.PostItem postItem = new PostListResponse.PostItem(
                "postId",
                "Test Title",
                "Test Content",
                List.of("tag1", "tag2"),
                "thumbnailUrl",
                100L,
                5L,
                10L,
                false, // 비로그인 상태에서는 좋아요 상태가 false
                false,
                new PostListResponse.AuthorInfo("authorId", "author@test.com", "authorName", "authorProfileImageUrl"),
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            
            PostListResponse mockResponse = new PostListResponse(
                List.of(postItem),
                false,
                null
            );
            
            when(postService.getPostsList(isNull(), eq("latest"), isNull(), isNull(), eq(20)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts[0].id").value("postId"))
                .andExpect(jsonPath("$.posts[0].isLiked").value(false))
                .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @DisplayName("정렬 옵션과 키워드로 검색")
    @Test
    public void getPostsWithSortAndKeyword() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            PostListResponse mockResponse = new PostListResponse(
                List.of(),
                false,
                null
            );
            
            when(postService.getPostsList(eq("userId"), eq("popular"), eq("test"), isNull(), eq(10)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                    .param("sortBy", "popular")
                    .param("keyword", "test")
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @DisplayName("limit 값이 50을 초과하면 50으로 제한")
    @Test
    public void getPostsWithLimitOver50() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            PostListResponse mockResponse = new PostListResponse(
                List.of(),
                false,
                null
            );
            
            when(postService.getPostsList(eq("userId"), eq("latest"), isNull(), isNull(), eq(50)))
                .thenReturn(mockResponse);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/posts")
                    .param("limit", "100")) // 100으로 요청하지만 50으로 제한됨
                .andExpect(status().isOk());
        }
    }
}