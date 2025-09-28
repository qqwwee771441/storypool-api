package com.wudc.storypool.domain.fairytale.controller.request;

import com.wudc.storypool.domain.fairytale.entity.constant.FairytaleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "동화 생성 완료 알림 요청")
public record NotifyFairytaleCompletionRequest(
    @Schema(description = "업데이트할 Fairytale 레코드의 고유 식별자", example = "01F8XYZABC123456DEF7890GHI")
    @NotBlank(message = "동화 ID는 필수입니다.")
    String id,
    
    @Schema(description = "생성된 페이지 총수", example = "5")
    @NotNull(message = "페이지 수는 필수입니다.")
    @Min(value = 1, message = "페이지 수는 1 이상이어야 합니다.")
    Integer pageNumber,
    
    @Schema(description = "각 페이지별 상세 객체 리스트")
    @NotNull(message = "페이지 리스트는 필수입니다.")
    @Valid
    List<PageData> pageList,
    
    @Schema(description = "최종 상태", example = "COMPLETED")
    @NotNull(message = "상태는 필수입니다.")
    FairytaleStatus status,
    
    @Schema(description = "완료 메시지", example = "동화 생성이 완료되었습니다.")
    @NotBlank(message = "메시지는 필수입니다.")
    @Size(max = 255, message = "메시지는 최대 255자까지 입력할 수 있습니다.")
    String message
) {
    @Schema(description = "페이지 데이터")
    public record PageData(
        @Schema(description = "페이지 순서 번호", example = "1")
        @NotNull(message = "페이지 인덱스는 필수입니다.")
        @Min(value = 0, message = "페이지 인덱스는 0 이상이어야 합니다.")
        Integer pageIndex,
        
        @Schema(description = "해당 페이지의 감정 태그", example = "happy")
        @NotBlank(message = "감정 태그는 필수입니다.")
        String mood,
        
        @Schema(description = "해당 페이지의 본문 텍스트", example = "첫 번째 페이지 내용…")
        @NotBlank(message = "스토리는 필수입니다.")
        String story,
        
        @Schema(description = "S3 등에 저장된 페이지 이미지 URL", 
                example = "https://bucket-name.s3.amazonaws.com/fairytale/01F8XYZ_PAGE1.png")
        @NotBlank(message = "이미지 URL은 필수입니다.")
        String imageUrl
    ) {}
}