package com.wudc.storypool.story;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.story.controller.request.UpdateStoryRequest;
import com.wudc.storypool.domain.story.entity.Story;
import com.wudc.storypool.domain.story.repository.StoryRepository;
import com.wudc.storypool.domain.story.service.StoryService;
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

import java.time.LocalDateTime;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("PATCH /api/stories/{id} 스토리 수정 테스트")
public class UpdateStoryTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StoryRepository storyRepository;

    @DisplayName("스토리 수정 성공")
    @Test
    public void updateStorySuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Story mockStory = Story.builder()
                .name("Updated Story")
                .text("Updated story content")
                .userId("userId")
                .isDeleted(false)
                .build();
            mockStory.setId("storyId");

            Story story = storyRepository.save(mockStory);

            // when
            UpdateStoryRequest request = new UpdateStoryRequest(
                "Updated Story",
                "Updated story content....................................................."
            );

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/stories/" + story.getId())
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.draftId").value("storyId"))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
        }
    }
}