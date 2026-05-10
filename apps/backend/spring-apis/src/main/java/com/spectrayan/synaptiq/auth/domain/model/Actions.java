package com.spectrayan.synaptiq.auth.domain.model;

/**
 * Action identifiers used in scope definitions.
 * <p>
 * Scopes follow the format {@code resource:action}.
 * Standard CRUD actions plus domain-specific ones.
 */
public final class Actions {
    private Actions() {}

    // ── Standard CRUD ────────────────────────────────────────────────
    public static final String CREATE = "create";
    public static final String READ   = "read";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String LIST   = "list";

    // ── Domain-specific ──────────────────────────────────────────────
    public static final String EXECUTE         = "execute";
    public static final String SHARE           = "share";
    public static final String DUPLICATE       = "duplicate";
    public static final String GENERATE        = "generate";
    public static final String SEND            = "send";
    public static final String DISMISS         = "dismiss";
    public static final String MANAGE_USERS    = "manage-users";
    public static final String UPDATE_SELF     = "update-self";
    public static final String CHANGE_PASSWORD = "change-password";
    public static final String IMPORT          = "import";
    public static final String QUERY           = "query";

    /** Wildcard — grants all actions for a resource. */
    public static final String ALL = "*";
}
