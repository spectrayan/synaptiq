import { ErrorCode } from './error-codes';

// Make sure to import the right ProblemDetails from the generated SDK
// import { ProblemDetails } from '@synaptiq/client';
// Since we don't know the exact import path, we'll use a type that matches the shape:
type ProblemDetails = {
  code?: string;
  detail?: string;
  [key: string]: any;
};

/**
 * User-friendly error messages keyed by the backend's machine-readable error code.
 *
 * The UI owns these strings — they can be freely reworded, translated, or
 * enriched without any backend coordination.
 *
 * Workflow:
 *  1. Backend sends ProblemDetails with `code` extension property
 *  2. Frontend looks up the code in this map
 *  3. Falls back to the backend's `detail` field if the code is unknown
 */
const codeMessages: Record<string, string> = {
  // ── Auth ─────────────────────────────────────────────────────────
  [ErrorCode.AUTHENTICATION_FAILED]:
    'Invalid credentials or session expired. Please log in again.',
  [ErrorCode.INSUFFICIENT_ROLE]:
    'You do not have permission to perform this action.',
  [ErrorCode.ACTION_DISABLED]:
    'This action is currently disabled for your account.',

  // ── Limits ───────────────────────────────────────────────────────
  [ErrorCode.RATE_LIMIT_EXCEEDED]:
    'You are making too many requests. Please slow down and try again later.',
  [ErrorCode.TENANT_LIMIT_EXCEEDED]:
    'Your workspace has reached its plan limits. Please upgrade to continue.',

  // ── AI ───────────────────────────────────────────────────────────
  [ErrorCode.LLM_ERROR]:
    'The AI model failed to process your request. Please try again.',

  // ── Generic ──────────────────────────────────────────────────────
  [ErrorCode.NOT_FOUND]:
    'The requested resource could not be found.',
  [ErrorCode.DUPLICATE_RESOURCE]:
    'A resource with this identifier already exists.',
  [ErrorCode.VALIDATION_ERROR]:
    'The provided information is invalid. Please check your input and try again.',
  [ErrorCode.INTERNAL_ERROR]:
    'An unexpected error occurred. Our team has been notified. Please try again later.',
};

/**
 * Resolves a user-friendly error message from a backend ProblemDetails response.
 *
 * Priority:
 *  1. Look up `code` in the code → message map
 *  2. Fall back to the backend's `detail` field
 *  3. Fall back to the provided default message
 *
 * @param error  The HTTP error response body (ProblemDetails from SDK, or any)
 * @param fallback  A default fallback message if nothing matches
 */
export function resolveErrorMessage(error: ProblemDetails | null | undefined, fallback: string): string;
export function resolveErrorMessage(error: any, fallback: string): string;
export function resolveErrorMessage(error: any, fallback: string): string {
  const code = error?.code as string | undefined;
  if (code && codeMessages[code]) {
    return codeMessages[code];
  }
  return error?.detail ?? fallback;
}

/**
 * Fallback messages used when the backend response is completely unavailable
 * (e.g. network error). These are NOT keyed by error code.
 */
export const FallbackMessages = {
  NETWORK_ERROR: 'Unable to connect to the server. Please check your internet connection.',
  LOAD_DATA: 'Failed to load data',
  SAVE_DATA: 'Failed to save changes',
  DELETE_DATA: 'Failed to delete resource',
} as const;
