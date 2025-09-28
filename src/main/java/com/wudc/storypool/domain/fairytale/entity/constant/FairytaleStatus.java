package com.wudc.storypool.domain.fairytale.entity.constant;

public enum FairytaleStatus {
    INIT,           // 초기 상태
    PENDING,        // 대기 중 (QUEUED 대신 사용)
    PROCESSING,     // 처리 중 (IN_PROGRESS 대신 사용)
    COMPLETED,      // 완료
    FAILED,         // 실패
    CANCELLED       // 취소
}