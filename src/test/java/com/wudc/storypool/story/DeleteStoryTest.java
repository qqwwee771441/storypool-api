package com.wudc.storypool.story;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.story.service.StoryService;
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
@DisplayName("DELETE /api/stories/{id} 스토리 삭제 테스트")
public class DeleteStoryTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private StoryService storyService;

    @DisplayName("스토리 삭제 성공")
    @Test
    public void deleteStorySuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            doNothing().when(storyService).deleteStory("userId", "storyId");

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/stories/storyId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Draft deleted successfully."));
        }
    }
}