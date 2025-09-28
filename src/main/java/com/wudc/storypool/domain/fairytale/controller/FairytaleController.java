package com.wudc.storypool.domain.fairytale.controller;

import com.wudc.storypool.common.util.AuthUtil;
import com.wudc.storypool.domain.fairytale.controller.request.*;
import com.wudc.storypool.domain.fairytale.controller.response.*;
import com.wudc.storypool.domain.fairytale.entity.Fairytale;
import com.wudc.storypool.domain.fairytale.entity.FairytaleePage;
import com.wudc.storypool.domain.fairytale.service.FairytaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "Fairytale", description = "AI 동화 생성 및 관리 API")
@RestController
@RequestMapping("/api/fairytales")
@RequiredArgsConstructor
public class FairytaleController {

    private final FairytaleService fairytaleService;

    @Operation(summary = "동화 목록 조회", description = "로그인한 사용자의 동화 목록을 커서 기반 페이징으로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "limit이 범위를 벗어남 (1~50)"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/my")
    public FairytaleListResponse getMyFairytales(
        @RequestParam(required = false) String after,
        @RequestParam(defaultValue = "20") int limit
    ) {
        if (limit > 50) limit = 50;
        if (limit < 1) limit = 1;

        String userId = AuthUtil.getUserId();
        List<Fairytale> fairytales = fairytaleService.getFairytalesList(userId, after, limit);
        
        // Check if there are more fairytales
        boolean hasNext = fairytales.size() == limit && 
                         fairytaleService.hasNextPage(userId, after, limit);
        
        // Get next cursor (last fairytale id if has next)
        String nextCursor = hasNext && !fairytales.isEmpty() ? 
                           fairytales.get(fairytales.size() - 1).getId() : null;

        List<FairytaleListResponse.FairytaleItem> fairytaleItems = fairytales.stream()
                .map(fairytale -> {
                    FairytaleePage thumbnailPage = fairytale.getThumbnailPage();
                    FairytaleListResponse.ThumbnailInfo thumbnail = null;
                    
                    if (thumbnailPage != null) {
                        thumbnail = new FairytaleListResponse.ThumbnailInfo(
                            thumbnailPage.getPageIndex(),
                            thumbnailPage.getMood(),
                            thumbnailPage.getStory(),
                            thumbnailPage.getImageUrl()
                        );
                    }
                    
                    return new FairytaleListResponse.FairytaleItem(
                        fairytale.getId(),
                        fairytale.getName(),
                        fairytale.getPageNumber(),
                        thumbnail,
                        fairytale.getStatus(),
                        fairytale.getMessage(),
                        fairytale.getCreatedAtByLocalDateTime(),
                        fairytale.getUpdatedAtByLocalDateTime()
                    );
                })
                .toList();

        return new FairytaleListResponse(fairytaleItems, hasNext, nextCursor);
    }

    @Operation(summary = "동화 상세 조회", description = "특정 동화의 모든 페이지 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "400", description = "id 형식이 유효하지 않음"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "다른 사용자의 동화를 조회하려 할 때"),
        @ApiResponse(responseCode = "404", description = "해당 동화를 찾을 수 없습니다"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/{id}")
    public FairytaleDetailResponse getFairytaleDetail(@PathVariable String id) {
        String userId = AuthUtil.getUserId();
        Fairytale fairytale = fairytaleService.getFairytaleDetail(userId, id);
        
        List<FairytaleDetailResponse.PageInfo> pageList = fairytale.getPageList().stream()
                .map(page -> new FairytaleDetailResponse.PageInfo(
                    page.getPageIndex(),
                    page.getMood(),
                    page.getStory(),
                    page.getImageUrl()
                ))
                .toList();
        
        return new FairytaleDetailResponse(
            fairytale.getId(),
            fairytale.getName(),
            fairytale.getPageNumber(),
            pageList,
            fairytale.getStatus(),
            fairytale.getMessage(),
            fairytale.getCreatedAtByLocalDateTime(),
            fairytale.getUpdatedAtByLocalDateTime()
        );
    }

    @Operation(summary = "동화 생성 시작", description = "스토리 ID를 기반으로 AI 동화 생성 작업을 시작합니다. 동일한 스토리에 대해 이미 생성 중인 동화가 있으면 기존 동화를 반환합니다. (멱등성 보장)")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "작업이 정상적으로 등록됨 또는 기존 진행 중인 작업 반환"),
        @ApiResponse(responseCode = "400", description = "요청 JSON 누락·형식 오류, storyId/name 검증 실패"),
        @ApiResponse(responseCode = "401", description = "인증 토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 스토리 ID 또는 접근 권한 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public GenerateFairytaleResponse generateFairytale(@RequestBody @Valid GenerateFairytaleRequest request) {
        String userId = AuthUtil.getUserId();
        Fairytale fairytale = fairytaleService.generateFairytale(userId, request.storyId(), request.name());
        
        return new GenerateFairytaleResponse(
            fairytale.getId(),
            fairytale.getStatus(),
            fairytale.getMessage()
        );
    }

    @Operation(summary = "동화 생성 상태 조회", description = "동화 생성 작업의 현재 진행 상태를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "현재 상태를 정상 조회"),
        @ApiResponse(responseCode = "400", description = "fairytaleId 형식 오류 또는 누락"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "본인 소유가 아닌 fairytaleId 조회 시"),
        @ApiResponse(responseCode = "404", description = "해당 fairytaleId를 가진 동화가 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @GetMapping("/{fairytaleId}/status")
    public FairytaleStatusResponse getFairytaleStatus(@PathVariable String fairytaleId) {
        String userId = AuthUtil.getUserId();
        Fairytale fairytale = fairytaleService.getFairytaleStatus(userId, fairytaleId);
        
        return new FairytaleStatusResponse(
            fairytale.getStatus(),
            fairytale.getMessage()
        );
    }


    @Operation(summary = "동화 수정", description = "동화 제목을 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "name이 빈 문자열이거나 길이 제한을 벗어날 때"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "소유자가 아닌 사용자가 수정 요청을 할 때"),
        @ApiResponse(responseCode = "404", description = "해당 id의 동화를 찾을 수 없을 때"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{id}")
    public Map<String, Object> updateFairytale(
        @PathVariable String id,
        @RequestBody @Valid UpdateFairytaleRequest request
    ) {
        String userId = AuthUtil.getUserId();
        Fairytale fairytale = fairytaleService.updateFairytale(userId, id, request.name());
        
        return Map.of(
            "id", fairytale.getId(),
            "name", fairytale.getName(),
            "updatedAt", fairytale.getUpdatedAt()
        );
    }

    @Operation(summary = "동화 삭제", description = "특정 동화를 완전 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 id 형식"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "소유자가 아닌 사용자가 삭제 요청을 할 때"),
        @ApiResponse(responseCode = "404", description = "해당 id의 동화를 찾을 수 없을 때"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @DeleteMapping("/{id}")
    public Map<String, Boolean> deleteFairytale(@PathVariable String id) {
        String userId = AuthUtil.getUserId();
        fairytaleService.deleteFairytale(userId, id);
        
        return Map.of("success", true);
    }

    @Operation(summary = "테스트 동화 생성", description = "원활한 테스트를 위한 완성된 템플릿 동화를 즉시 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "테스트 동화 생성 성공"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/generate-test-fairytale")
    @ResponseStatus(HttpStatus.CREATED)
    public GenerateFairytaleResponse generateTestFairytale() {
        String userId = AuthUtil.getUserId();
        Fairytale fairytale = fairytaleService.generateTestFairytale(userId);
        
        return new GenerateFairytaleResponse(
            fairytale.getId(),
            fairytale.getStatus(),
            fairytale.getMessage()
        );
    }

    @Operation(summary = "동화 생성 상태 수정", description = "워커 프로세스가 동화 생성 진행 상태를 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 업데이트 성공"),
        @ApiResponse(responseCode = "400", description = "요청 바디 누락·오류 or 잘못된 status 값"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "해당 fairytaleId에 대한 수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "해당 fairytaleId 동화가 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PatchMapping("/{fairytaleId}/status")
    public UpdateFairytaleStatusResponse updateFairytaleStatus(
        @PathVariable String fairytaleId,
        @RequestBody @Valid UpdateFairytaleStatusRequest request
    ) {
        Fairytale fairytale = fairytaleService.updateFairytaleStatus(
            fairytaleId, 
            request.status(), 
            request.message()
        );
        
        return new UpdateFairytaleStatusResponse(
            fairytale.getId(),
            fairytale.getStatus(),
            fairytale.getMessage(),
            fairytale.getUpdatedAtByLocalDateTime()
        );
    }

    @Operation(summary = "동화 생성 완료", description = "워커 프로세스가 동화 생성 완료를 알리고 페이지 데이터를 업데이트합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "알림(완료) 처리 성공"),
        @ApiResponse(responseCode = "400", description = "요청 바디 누락·검증 오류"),
        @ApiResponse(responseCode = "401", description = "토큰 누락 또는 유효하지 않음"),
        @ApiResponse(responseCode = "403", description = "워커 계정에 권한 없음"),
        @ApiResponse(responseCode = "404", description = "해당 id의 동화가 존재하지 않음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @SecurityRequirement(name = "jwtAuth")
    @PostMapping("/notify")
    public NotifyFairytaleCompletionResponse notifyFairytaleCompletion(
        @RequestBody @Valid NotifyFairytaleCompletionRequest request
    ) {
        Fairytale fairytale = fairytaleService.notifyFairytaleCompletion(request);
        
        return new NotifyFairytaleCompletionResponse(
            fairytale.getId(),
            fairytale.getStatus(),
            fairytale.getMessage(),
            fairytale.getUpdatedAtByLocalDateTime()
        );
    }
}