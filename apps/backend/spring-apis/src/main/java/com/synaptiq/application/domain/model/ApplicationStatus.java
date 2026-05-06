package com.synaptiq.application.domain.model;

/**
 * Application lifecycle status.
 */
public enum ApplicationStatus {
    /** Being configured, not yet accessible to end users. */
    DRAFT,
    /** Live and accessible to end users. */
    ACTIVE,
    /** Temporarily paused — not accessible but config preserved. */
    PAUSED,
    /** Permanently retired — kept for audit trail only. */
    ARCHIVED
}
