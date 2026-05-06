package com.synaptiq.shared.exception;

/**
 * Application-level error codes for structured error responses.
 */
public enum ErrorCode {
    NOT_FOUND,
    DUPLICATE_RESOURCE,
    VALIDATION_ERROR,
    AUTHENTICATION_FAILED,
    INSUFFICIENT_ROLE,
    RATE_LIMIT_EXCEEDED,
    TENANT_LIMIT_EXCEEDED,
    LLM_ERROR,
    ACTION_DISABLED,
    INTERNAL_ERROR
}
