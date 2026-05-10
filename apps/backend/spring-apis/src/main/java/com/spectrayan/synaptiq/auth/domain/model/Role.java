package com.spectrayan.synaptiq.auth.domain.model;

import com.spectrayan.synaptiq.shared.domain.AggregateRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

/**
 * Role aggregate — a named bundle of scopes.
 * <p>
 * Roles can be:
 * <ul>
 *   <li><b>System roles</b> ({@code systemRole = true}) — seeded on startup, immutable</li>
 *   <li><b>Custom roles</b> ({@code systemRole = false}) — created by tenant admins, tenant-scoped</li>
 * </ul>
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Role extends AggregateRoot {
    /** Stable identifier, e.g. "tenant:admin". Primary lookup key. */
    private String slug;
    /** Owning tenant. null = global (system) role. */
    private String tenantId;
    /** Human-readable name for admin UI. */
    private String displayName;
    /** Description for admin UI. */
    private String description;
    /** Whether this role is system-managed and cannot be edited/deleted. */
    private boolean systemRole;
    /** Role type: "global" or "tenant". */
    private String roleType;
    /** Set of scope slugs this role grants. */
    private Set<String> scopeSlugs;
}
