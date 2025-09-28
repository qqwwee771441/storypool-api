package com.wudc.storypool.comment;

import com.wudc.storypool.common.util.AuthUtil;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("POST /api/comments/{commentId}/like 댓글 좋아요 토글 테스트")
public class CommentLikeTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CommentService commentService;

    @DisplayName("댓글 좋아요 토글 성공")
    @Test
    public void toggleCommentLikeSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.post("/api/comments/commentId/like"))
                .andExpect(status().isNoContent());
        }
    }
}