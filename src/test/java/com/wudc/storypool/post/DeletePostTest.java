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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("DELETE /api/posts/{id} 게시글 삭제 테스트")
public class DeletePostTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PostService postService;

    @DisplayName("게시글 삭제 성공")
    @Test
    public void deletePostSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            doNothing().when(postService).deletePost("userId", "postId");

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/posts/postId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Post deleted successfully."));
        }
    }
}