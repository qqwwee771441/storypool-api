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
@DisplayName("DELETE /api/comments/{commentId} 댓글 삭제 테스트")
public class DeleteCommentTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CommentService commentService;

    @DisplayName("댓글 삭제 성공")
    @Test
    public void deleteCommentSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            doNothing().when(commentService).deleteComment("userId", "commentId");

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/comments/commentId"))
                .andExpect(status().isNoContent());
        }
    }
}