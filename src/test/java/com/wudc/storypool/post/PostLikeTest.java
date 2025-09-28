package com.wudc.storypool.post;

import com.wudc.storypool.common.util.AuthUtil;
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

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /api/posts/{id}/like 게시글 좋아요 토글 테스트")
public class PostLikeTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PostService postService;

    @DisplayName("좋아요 추가 성공")
    @Test
    public void toggleLikeAddSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            when(postService.toggleLike("userId", "postId")).thenReturn(true);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/postId/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(true))
                .andExpect(jsonPath("$.message").value("좋아요가 추가되었습니다."));
        }
    }

    @DisplayName("좋아요 취소 성공")
    @Test
    public void toggleLikeCancelSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            when(postService.toggleLike("userId", "postId")).thenReturn(false);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post("/api/posts/postId/like"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(false))
                .andExpect(jsonPath("$.message").value("좋아요가 취소되었습니다."));
        }
    }
}