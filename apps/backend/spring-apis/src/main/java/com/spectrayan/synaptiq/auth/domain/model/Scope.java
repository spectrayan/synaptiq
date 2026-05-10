package com.spectrayan.synaptiq.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single permission unit — "{@code resource:action}" format.
 * <p>
 * Scopes are seeded on startup and stored in MongoDB.
 * They define what permissions <em>exist</em> in the system.
 * Roles reference them by slug.
 */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Scope {
    /** Unique identifier, e.g. "workflow:create". */
    private String slug;
    /** Human-readable name for admin UI, e.g. "Create Workflow". */
    private String displayName;
    /** Description for admin UI tooltips. */
    private String description;
    /** Resource category, e.g. "workflow". */
    private String resource;
    /** Action within the resource, e.g. "create". */
    private String action;
}
