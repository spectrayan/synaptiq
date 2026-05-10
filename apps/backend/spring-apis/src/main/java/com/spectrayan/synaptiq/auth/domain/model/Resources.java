package com.spectrayan.synaptiq.auth.domain.model;

/**
 * Resource identifiers used in scope definitions.
 * <p>
 * Each resource corresponds to a bounded context in the system.
 * Scopes follow the format {@code resource:action}.
 */
public final class Resources {
    private Resources() {}

    public static final String APPLICATION     = "application";
    public static final String WORKFLOW        = "workflow";
    public static final String DATASOURCE      = "datasource";
    public static final String CHAT            = "chat";
    public static final String BRANDING        = "branding";
    public static final String TENANT_CONFIG   = "tenant-config";
    public static final String TENANT          = "tenant";
    public static final String USER            = "user";
    public static final String NOTIFICATION    = "notification";
    public static final String ROLE            = "role";
    public static final String INTEGRATION     = "integration";
    public static final String ANALYTICS       = "analytics";
    public static final String SCHEMA_REGISTRY = "schema-registry";
}
