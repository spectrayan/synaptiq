package com.spectrayan.synaptiq.integration.model;

/**
 * Lifecycle status of a tenant integration route.
 */
public enum RouteStatus {
    /** Created but not yet activated. */
    PENDING,
    /** Route is loaded and running in the CamelContext. */
    ACTIVE,
    /** Route failed to load or last execution errored. */
    ERROR,
    /** Manually disabled by tenant admin. */
    DISABLED
}
