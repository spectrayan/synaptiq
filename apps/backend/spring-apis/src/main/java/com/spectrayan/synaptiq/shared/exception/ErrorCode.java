package com.spectrayan.synaptiq.shared.exception;

/**
 * Machine-readable error codes sent in the {@code code} extension property
 * of RFC 9457 Problem Details responses.
 * <p>
 * The frontend uses these codes to look up its own user-friendly, localisable
 * error messages — the backend's {@code detail} field is a fallback only.
 * <p>
 * Convention: {@code DOMAIN_VERB_REASON} in UPPER_SNAKE_CASE.
 */
public final class ErrorCode {

    private ErrorCode() {} // utility class

    // ═══════════════════════════════════════════════════════════════════
    // Auth
    // ═══════════════════════════════════════════════════════════════════

    public static final String AUTHENTICATION_FAILED  = "AUTHENTICATION_FAILED";
    public static final String INSUFFICIENT_ROLE      = "INSUFFICIENT_ROLE";
    public static final String ACTION_DISABLED        = "ACTION_DISABLED";

    // ═══════════════════════════════════════════════════════════════════
    // Limits
    // ═══════════════════════════════════════════════════════════════════

    public static final String RATE_LIMIT_EXCEEDED    = "RATE_LIMIT_EXCEEDED";
    public static final String TENANT_LIMIT_EXCEEDED  = "TENANT_LIMIT_EXCEEDED";

    // ═══════════════════════════════════════════════════════════════════
    // AI
    // ═══════════════════════════════════════════════════════════════════

    public static final String LLM_ERROR              = "LLM_ERROR";

    // ═══════════════════════════════════════════════════════════════════
    // Generic
    // ═══════════════════════════════════════════════════════════════════

    public static final String NOT_FOUND              = "NOT_FOUND";
    public static final String DUPLICATE_RESOURCE     = "DUPLICATE_RESOURCE";
    public static final String VALIDATION_ERROR       = "VALIDATION_ERROR";
    public static final String INTERNAL_ERROR         = "INTERNAL_ERROR";
}
