package com.aiproxy.exception;

import com.aiproxy.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(
        RateLimitExceededException exception,
        HttpServletRequest request
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.RETRY_AFTER, String.valueOf(exception.getRetryAfterSeconds()));

        ApiErrorResponse body = buildErrorBody(
            HttpStatus.TOO_MANY_REQUESTS,
            exception.getMessage(),
            request
        );

        return new ResponseEntity<>(body, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleQuotaExceeded(
        QuotaExceededException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse body = buildErrorBody(
            HttpStatus.PAYMENT_REQUIRED,
            exception.getMessage(),
            request
        );

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
        UserNotFoundException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse body = buildErrorBody(HttpStatus.NOT_FOUND, exception.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
        IllegalArgumentException exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse body = buildErrorBody(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
        Exception exception,
        HttpServletRequest request
    ) {
        ApiErrorResponse body = buildErrorBody(
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.getMessage(),
            request
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ApiErrorResponse buildErrorBody(HttpStatus status, String message, HttpServletRequest request) {
        return ApiErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .path(request.getRequestURI())
            .build();
    }
}

