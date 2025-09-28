package com.wudc.storypool.fairytale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.fairytale.controller.request.GenerateFairytaleRequest;
import com.wudc.storypool.domain.fairytale.entity.Fairytale;
import com.wudc.storypool.domain.fairytale.service.FairytaleService;
import com.wudc.storypool.domain.story.entity.Story;
import com.wudc.storypool.domain.story.repository.StoryRepository;
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
@DisplayName("POST /api/fairytales/generate 동화 생성 테스트")
public class GenerateFairytaleTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StoryRepository storyRepository;

//    @DisplayName("동화 생성 성공")
//    @Test
//    public void generateFairytaleSuccess() throws Exception {
//        // given
//        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
//            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
//
//            Fairytale mockFairytale = new Fairytale();
//            mockFairytale.setId("fairytaleId");
//            mockFairytale.setName("Test Fairytale");
//
//            Story story = new Story(
//                "userId",
//                "Test Fairytale",
//                "This is a test story content that is long enough to meet the minimum requirement of 50 characters for fairytale generation.",
//                false
//            );
//            story.setId("storyId");
//
//            storyRepository.save(story);
//
//            // when
//            GenerateFairytaleRequest request = new GenerateFairytaleRequest("storyId", "testName");
//
//            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/fairytales/generate")
//                .content(new ObjectMapper().writeValueAsString(request))
//                .contentType("application/json");
//
//            // then
//            mockMvc.perform(requestBuilder)
//                .andExpect(status().isAccepted())
//                .andExpect(jsonPath("$.fairytaleId").value("fairytaleId"));
//        }
//    }

    @DisplayName("동화 ID를 빈 문자열로 요청")
    @Test
    public void generateFairytaleWithEmptyId() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");

            Fairytale mockFairytale = new Fairytale();
            mockFairytale.setId("fairytaleId");
            mockFairytale.setName("Test Fairytale");

            Story story = new Story(
                "userId",
                "Test Fairytale",
                "This is a test story content that is long enough to meet the minimum requirement of 50 characters for fairytale generation.",
                false
            );
            story.setId("storyId");

            storyRepository.save(story);

            // when
            GenerateFairytaleRequest request = new GenerateFairytaleRequest("", "testName");

            MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/fairytales/generate")
                .content(new ObjectMapper().writeValueAsString(request))
                .contentType("application/json");

            // then
            mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("storyId는 필수입니다."));
        }
    }
}