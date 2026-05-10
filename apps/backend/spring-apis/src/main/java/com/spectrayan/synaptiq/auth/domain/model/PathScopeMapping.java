package com.spectrayan.synaptiq.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps an HTTP method + URL path pattern to a required scope.
 * <p>
 * These are seeded on startup and stored in MongoDB.
 * The {@link com.spectrayan.synaptiq.auth.infrastructure.security.ScopeAuthorizationManager}
 * uses them to decide which scope is required for each API request.
 * <p>
 * Path patterns follow Spring's {@code PathPattern} syntax:
 * <ul>
 *   <li>{@code /api/v1/workflows} — exact path</li>
 *   <li>{@code /api/v1/workflows/{id}} — path with variable</li>
 *   <li>{@code /api/v1/workflows/{id}/execute} — nested action</li>
 * </ul>
 */
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PathScopeMapping {
    private String id;
    /** HTTP method: GET, POST, PUT, PATCH, DELETE. */
    private String httpMethod;
    /** Spring PathPattern, e.g. "/api/v1/workflows/{id}". */
    private String pathPattern;
    /** Required scope slug, e.g. "workflow:create". */
    private String requiredScope;
}
