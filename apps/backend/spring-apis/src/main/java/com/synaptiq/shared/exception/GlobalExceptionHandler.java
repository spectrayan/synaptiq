package com.synaptiq.shared.exception;

import lombok.extern.slf4j.Slf4j;
import com.synaptiq.infrastructure.in.web.dto.ProblemDetails;
import com.synaptiq.infrastructure.in.web.dto.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import com.synaptiq.shared.util.ExceptionUtils;

import java.net.URI;
import java.time.Instant;

/**
 * Global exception handler — translates domain exceptions into
 * RFC 9457 Problem Details responses.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9457">RFC 9457</a>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String PROBLEM_TYPE_BASE = "https://api.synaptiq.com/problems/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleNotFound(ResourceNotFoundException ex) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ProblemDetails> handleDuplicate(DuplicateResourceException ex) {
        return buildProblemDetail(HttpStatus.CONFLICT, ex.getMessage(), ex.getErrorCode());
    }

    @ExceptionHandler(SynaptiqException.class)
    public ResponseEntity<ProblemDetails> handleSynaptiq(SynaptiqException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case ErrorCode.AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED;
            case ErrorCode.INSUFFICIENT_ROLE -> HttpStatus.FORBIDDEN;
            case ErrorCode.RATE_LIMIT_EXCEEDED, ErrorCode.TENANT_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case ErrorCode.VALIDATION_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ErrorCode.ACTION_DISABLED -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return buildProblemDetail(status, ex.getMessage(), ex.getErrorCode());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetails> handleValidation(WebExchangeBindException ex) {
        var firstError = ex.getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .findFirst()
            .orElse(ex.getMessage());
        return buildProblemDetail(HttpStatus.BAD_REQUEST, firstError, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ProblemDetails> handleInput(ServerWebInputException ex) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getReason(), ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ExceptionUtils.getRootCauseMessage(ex));
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error", ErrorCode.INTERNAL_ERROR);
    }

    private ResponseEntity<ProblemDetails> buildProblemDetail(HttpStatus status, String detail, String code) {
        ProblemDetails problem = new ProblemDetails()
            .type(URI.create(PROBLEM_TYPE_BASE + code.toLowerCase().replace("_", "-")))
            .title(status.getReasonPhrase())
            .status(status.value())
            .detail(detail != null ? detail : "Unknown error")
            .code(code)
            .timestamp(Instant.now().atOffset(java.time.ZoneOffset.UTC));
            
        return ResponseEntity.status(status)
            .header("Content-Type", "application/problem+json")
            .body(problem);
    }
}
