/**
 * GlobalErrorHandler — catches uncaught exceptions across the application.
 *
 * Prevents the app from crashing on unexpected errors by:
 *   1. Logging to console with structured context
 *   2. Optionally displaying a toast/snackbar (extensible)
 *   3. Filtering known benign errors (e.g., ResizeObserver, AbortError)
 *
 * This replaces Angular's default ErrorHandler to provide resilient UX.
 */
import { ErrorHandler, Injectable } from '@angular/core';

/** Errors that are expected/benign and should not trigger UI feedback. */
const IGNORED_PATTERNS = [
  'ResizeObserver loop',           // Benign browser warning
  'AbortError',                    // SSE stream aborted by user
  'Loading chunk',                 // Lazy-load retry will handle
  'ExpressionChangedAfterItHas',   // Dev-mode only
];

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  handleError(error: unknown): void {
    const message = error instanceof Error ? error.message : String(error);

    // Skip benign errors
    if (IGNORED_PATTERNS.some((p) => message.includes(p))) {
      return;
    }

    // Structured console logging
    console.error('[Synaptiq Error]', {
      message,
      timestamp: new Date().toISOString(),
      stack: error instanceof Error ? error.stack : undefined,
    });

    // TODO: Send to remote error tracking (e.g., Sentry, Cloud Logging)
    // this.errorReporter.report(error);
  }
}
