package com.synaptiq.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

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
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return buildProblemDetail(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getErrorCode());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return buildProblemDetail(HttpStatus.CONFLICT, ex.getMessage(), ex.getErrorCode());
    }

    @ExceptionHandler(SynaptiqException.class)
    public ProblemDetail handleSynaptiq(SynaptiqException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case AUTHENTICATION_FAILED -> HttpStatus.UNAUTHORIZED;
            case INSUFFICIENT_ROLE -> HttpStatus.FORBIDDEN;
            case RATE_LIMIT_EXCEEDED, TENANT_LIMIT_EXCEEDED -> HttpStatus.TOO_MANY_REQUESTS;
            case VALIDATION_ERROR -> HttpStatus.UNPROCESSABLE_ENTITY;
            case ACTION_DISABLED -> HttpStatus.FORBIDDEN;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return buildProblemDetail(status, ex.getMessage(), ex.getErrorCode());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ProblemDetail handleValidation(WebExchangeBindException ex) {
        var firstError = ex.getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .findFirst()
            .orElse(ex.getMessage());
        return buildProblemDetail(HttpStatus.BAD_REQUEST, firstError, ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ProblemDetail handleInput(ServerWebInputException ex) {
        return buildProblemDetail(HttpStatus.BAD_REQUEST, ex.getReason(), ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error", ErrorCode.INTERNAL_ERROR);
    }

    private ProblemDetail buildProblemDetail(HttpStatus status, String detail, ErrorCode code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail != null ? detail : "Unknown error");
        problem.setType(URI.create(PROBLEM_TYPE_BASE + code.name().toLowerCase()));
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("errorCode", code.name());
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
