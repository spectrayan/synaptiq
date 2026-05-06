package com.synaptiq.datasource.domain.model;

/**
 * DataSource lifecycle status.
 */
public enum DataSourceStatus {
    /** Registered but not yet validated/connected. */
    PENDING,
    /** Connection verified and schema inferred. */
    CONNECTED,
    /** Connection failed or credentials expired. */
    ERROR,
    /** Manually disabled by admin. */
    DISABLED
}
