package com.spharos.payment.exception;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.spharos.payment.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("Business exception: code={}, message={}", errorCode.getCode(), exception.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException exception) {
        log.warn("API not found: {}", exception.getResourcePath());
        ErrorCode errorCode = ErrorCode.RESOURCE_NOT_FOUND;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected exception", exception);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.failure(errorCode.getCode(), errorCode.getMessage()));
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
