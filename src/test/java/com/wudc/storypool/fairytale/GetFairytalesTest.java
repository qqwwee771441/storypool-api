package com.wudc.storypool.fairytale;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.fairytale.entity.Fairytale;
import com.wudc.storypool.domain.fairytale.service.FairytaleService;
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
@DisplayName("GET /api/fairytales/my 내 동화 목록 조회 테스트")
public class GetFairytalesTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FairytaleService fairytaleService;

    @DisplayName("내 동화 목록 조회 성공")
    @Test
    public void getMyFairytalesSuccess() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Fairytale mockFairytale = new Fairytale();
            mockFairytale.setId("fairytaleId");
            mockFairytale.setName("Test Fairytale");
            
            when(fairytaleService.getFairytalesList(eq("userId"), isNull(), eq(20)))
                .thenReturn(List.of(mockFairytale));
            when(fairytaleService.hasNextPage(eq("userId"), isNull(), eq(20)))
                .thenReturn(false);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/fairytales/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fairytales").isArray())
                .andExpect(jsonPath("$.fairytales[0].id").value("fairytaleId"))
                .andExpect(jsonPath("$.fairytales[0].name").value("Test Fairytale"))
                .andExpect(jsonPath("$.hasNext").value(false));
        }
    }

    @DisplayName("페이지네이션이 있는 경우")
    @Test
    public void getMyFairytalesWithPagination() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            Fairytale mockFairytale = new Fairytale();
            mockFairytale.setId("fairytaleId");
            mockFairytale.setName("Test Fairytale");
            
            when(fairytaleService.getFairytalesList(eq("userId"), eq("cursor123"), eq(1)))
                .thenReturn(List.of(mockFairytale));
            when(fairytaleService.hasNextPage(eq("userId"), eq("cursor123"), eq(1)))
                .thenReturn(true);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/fairytales/my")
                    .param("after", "cursor123")
                    .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fairytales").isArray())
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("fairytaleId"));
        }
    }

    @DisplayName("limit 값이 50을 초과하면 50으로 제한")
    @Test
    public void getMyFairytalesWithLimitOver50() throws Exception {
        // given
        try (var mockedAuthUtil = mockStatic(AuthUtil.class)) {
            mockedAuthUtil.when(AuthUtil::getUserId).thenReturn("userId");
            
            when(fairytaleService.getFairytalesList(eq("userId"), isNull(), eq(50)))
                .thenReturn(List.of());
            when(fairytaleService.hasNextPage(eq("userId"), isNull(), eq(50)))
                .thenReturn(false);

            // when & then
            mockMvc.perform(MockMvcRequestBuilders.get("/api/fairytales/my")
                    .param("limit", "100")) // 100으로 요청하지만 50으로 제한됨
                .andExpect(status().isOk());
        }
    }
}