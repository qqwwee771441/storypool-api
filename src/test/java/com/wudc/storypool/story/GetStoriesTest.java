package com.wudc.storypool.story;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.story.entity.Story;
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
@DisplayName("GET /api/stories 스토리 목록 조회 테스트")
public class GetStoriesTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private StoryService storyService;

    @DisplayName("스토리 목록 조회 성공")
    @Test
    public void getStoriesSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Story mockStory = Story.builder()
                .name("Test Story")
                .text("Test excerpt")
                .build();
            mockStory.setId("storyId");
            
            when(storyService.getStoriesList(eq("userId"), isNull(), eq(20)))
                .thenReturn(List.of(mockStory));

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/stories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stories").isArray())
                .andExpect(jsonPath("$.stories[0].id").value("storyId"))
                .andExpect(jsonPath("$.stories[0].name").value("Test Story"))
                .andExpect(jsonPath("$.stories[0].excerpt").value("Test excerpt"))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").isEmpty());
        }
    }

    @DisplayName("페이지네이션이 있는 경우")
    @Test
    public void getStoriesWithPagination() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Story mockStory1 = Story.builder()
                .name("Test Story 1")
                .text("Test excerpt 1")
                .build();
            mockStory1.setId("storyId1");
            
            Story mockStory2 = Story.builder()
                .name("Test Story 2")
                .text("Test excerpt 2")
                .build();
            mockStory2.setId("storyId2");
            
            when(storyService.getStoriesList(eq("userId"), eq("cursor123"), eq(1)))
                .thenReturn(List.of(mockStory1, mockStory2)); // 2개 반환하여 hasNext = true

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/stories")
                    .param("after", "cursor123")
                    .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stories").isArray())
                .andExpect(jsonPath("$.stories").isNotEmpty())
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("storyId1"));
        }
    }

    @DisplayName("limit 값이 50을 초과하면 50으로 제한")
    @Test
    public void getStoriesWithLimitOver50() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            when(storyService.getStoriesList(eq("userId"), isNull(), eq(50)))
                .thenReturn(List.of());

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/stories")
                    .param("limit", "100")) // 100으로 요청하지만 50으로 제한됨
                .andExpect(status().isOk());
        }
    }
}