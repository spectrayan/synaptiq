/**
 * Machine-readable error codes received from the backend's RFC 9457
 * ProblemDetails `code` extension property.
 *
 * Must stay in sync with `ErrorCode.java` on the backend.
 */
export const ErrorCode = {
  // ── Auth ─────────────────────────────────────────────────────────
  AUTHENTICATION_FAILED: 'AUTHENTICATION_FAILED',
  INSUFFICIENT_ROLE: 'INSUFFICIENT_ROLE',
  ACTION_DISABLED: 'ACTION_DISABLED',

  // ── Limits ───────────────────────────────────────────────────────
  RATE_LIMIT_EXCEEDED: 'RATE_LIMIT_EXCEEDED',
  TENANT_LIMIT_EXCEEDED: 'TENANT_LIMIT_EXCEEDED',

  // ── AI ───────────────────────────────────────────────────────────
  LLM_ERROR: 'LLM_ERROR',

  // ── Generic ──────────────────────────────────────────────────────
  NOT_FOUND: 'NOT_FOUND',
  DUPLICATE_RESOURCE: 'DUPLICATE_RESOURCE',
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  INTERNAL_ERROR: 'INTERNAL_ERROR',
} as const;

export type ErrorCodeType = typeof ErrorCode[keyof typeof ErrorCode];
