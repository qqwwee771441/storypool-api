package com.wudc.storypool.common.exception;

import com.wudc.storypool.common.base.BaseErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getStatus())
            .body(new BaseErrorResponse(ErrorCode.FORBIDDEN.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<BaseErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("Authorization denied: {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.FORBIDDEN.getStatus())
            .body(new BaseErrorResponse(ErrorCode.FORBIDDEN.getMessage()));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseErrorResponse> handleBaseException(BaseException e) {
        if (e.getErrorCode().getStatus().is5xxServerError()) {
            log.error("Server error occurred: {}", e.getErrorCode().getMessage(), e);
        } else {
            log.warn("Client error occurred: {}", e.getErrorCode().getMessage());
        }
        
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(new BaseErrorResponse(e.getErrorCode().getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        if (message == null) {
            message = "Invalid email format";
        }
        
        log.warn("Validation error: {}", message);
        
        return ResponseEntity
                .badRequest()
                .body(new BaseErrorResponse(message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getResourcePath());
        
        return ResponseEntity
                .status(ErrorCode.RESOURCE_NOT_FOUND.getStatus())
                .body(new BaseErrorResponse(ErrorCode.RESOURCE_NOT_FOUND.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected server error occurred", e);
        
        return ResponseEntity
                .internalServerError()
                .body(new BaseErrorResponse("Internal server error. Please try again later."));
    }
}
