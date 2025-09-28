package com.wudc.storypool.story;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.story.controller.request.CreateStoryRequest;
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
@DisplayName("POST /api/stories 스토리 생성 테스트")
public class CreateStoryTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StoryRepository storyRepository;

    @DisplayName("성공")
    @Test
    public void createStorySuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Story mockStory = Story.builder()
                .name("Test Story")
                .text("Test story content")
                .userId("userId")
                .isDeleted(false)
                .build();

            storyRepository.save(mockStory);

            // when
            CreateStoryRequest request = new CreateStoryRequest(
                "Test Story",
                "Test story content..................................."
            );

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/stories")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.draftId").isNotEmpty())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
        }
    }

    @DisplayName("이름이 빈 문자열로 요청")
    @Test
    public void createStoryWithEmptyName() throws Exception {
        // when
        CreateStoryRequest request = new CreateStoryRequest(
            "",
            "a".repeat(51)
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/stories")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("name은 2자 이상 100자 이하여야 합니다."));
    }

    @DisplayName("내용이 빈 문자열로 요청")
    @Test
    public void createStoryWithEmptyText() throws Exception {
        // when
        CreateStoryRequest request = new CreateStoryRequest(
            "Test Story",
            ""
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/stories")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("text는 50자 이상 5000자 이하여야 합니다."));
    }

    @DisplayName("이름이 너무 긴 경우")
    @Test
    public void createStoryWithLongName() throws Exception {
        // when - 201자 이름 (200자 초과)
        String longName = "a".repeat(201);
        CreateStoryRequest request = new CreateStoryRequest(
            longName,
            "a".repeat(51)
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/stories")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("name은 2자 이상 100자 이하여야 합니다."));
    }

    @DisplayName("내용이 너무 긴 경우")
    @Test
    public void createStoryWithLongText() throws Exception {
        // when - 5001자 내용 (5000자 초과)
        String longText = "a".repeat(5001);
        CreateStoryRequest request = new CreateStoryRequest(
            "Test Story",
            longText
        );

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/stories")
            .content(new ObjectMapper().writeValueAsString(request))
            .contentType("application/json");

        // then
        mockMvc.perform(requestBuilder)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("text는 50자 이상 5000자 이하여야 합니다."));
    }
}