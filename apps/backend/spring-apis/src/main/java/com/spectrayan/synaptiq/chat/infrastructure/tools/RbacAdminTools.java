package com.spectrayan.synaptiq.chat.infrastructure.tools;

import com.spectrayan.synaptiq.auth.application.port.in.RoleManagementUseCase;
import com.spectrayan.synaptiq.auth.domain.model.Role;
import com.spectrayan.synaptiq.auth.domain.model.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring AI tools for RBAC role management via chat.
 * <p>
 * These tools allow tenant admins to manage roles and scopes through
 * natural language in the chat interface ("Chat as Operating System").
 * <p>
 * The LLM will invoke these tools when the user asks things like:
 * <ul>
 *   <li>"Show me all roles"</li>
 *   <li>"Create a new role called marketing-viewer with read-only access to analytics"</li>
 *   <li>"What permissions does the editor role have?"</li>
 *   <li>"Remove the custom-reviewer role"</li>
 *   <li>"List all available permissions"</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacAdminTools {

    private final RoleManagementUseCase roleManagement;

    @Tool(description = "List all roles available for a tenant including system roles (admin, editor, viewer) and custom roles. " +
                        "Use when the user asks to see roles, permissions setup, or user access configuration.")
    public String listRoles(
            @ToolParam(description = "The tenant ID to list roles for. Use the current user's tenant ID.") String tenantId) {
        try {
            List<Role> roles = roleManagement.listRoles(tenantId).collectList().block();
            if (roles == null || roles.isEmpty()) {
                return "No roles found for this tenant.";
            }
            StringBuilder sb = new StringBuilder("**Roles (" + roles.size() + "):**\n\n");
            for (Role role : roles) {
                sb.append("- **").append(role.getDisplayName()).append("** (`").append(role.getSlug()).append("`)")
                  .append(role.isSystemRole() ? " 🔒 system" : " ✏️ custom")
                  .append("\n  Scopes: ");
                if (role.getScopeSlugs() != null) {
                    sb.append(String.join(", ", role.getScopeSlugs()));
                }
                sb.append("\n\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to list roles", e);
            return "Error listing roles: " + e.getMessage();
        }
    }

    @Tool(description = "Get detailed information about a specific role including its slug, display name, description, and all granted permission scopes. " +
                        "Use when the user asks about a specific role's permissions or capabilities.")
    public String getRole(
            @ToolParam(description = "The role slug, e.g. 'tenant:editor' or 'custom:marketing-viewer'") String roleSlug) {
        try {
            Role role = roleManagement.getRole(roleSlug).block();
            if (role == null) {
                return "Role not found: " + roleSlug;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("**").append(role.getDisplayName()).append("** (`").append(role.getSlug()).append("`)\n\n");
            sb.append("- **Type:** ").append(role.isSystemRole() ? "System (immutable)" : "Custom (editable)").append("\n");
            sb.append("- **Description:** ").append(role.getDescription() != null ? role.getDescription() : "—").append("\n");
            sb.append("- **Permissions:**\n");
            if (role.getScopeSlugs() != null) {
                role.getScopeSlugs().stream().sorted().forEach(scope ->
                    sb.append("  - `").append(scope).append("`\n"));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Create a new custom role for the tenant with specific permission scopes. " +
                        "Use when the user wants to create a new role with specific access. " +
                        "The slug must be unique and follow the format 'custom:name'. " +
                        "Scope slugs follow 'resource:action' format, e.g. 'workflow:create', 'chat:send', 'analytics:read'.")
    public String createRole(
            @ToolParam(description = "Tenant ID for the new role") String tenantId,
            @ToolParam(description = "Unique role slug, e.g. 'custom:marketing-viewer'") String slug,
            @ToolParam(description = "Human-readable name, e.g. 'Marketing Viewer'") String displayName,
            @ToolParam(description = "Role description") String description,
            @ToolParam(description = "Comma-separated scope slugs, e.g. 'analytics:read,datasource:list,chat:send'") String scopeSlugsCSV) {
        try {
            Set<String> scopes = new HashSet<>(List.of(scopeSlugsCSV.split(",")));
            scopes = scopes.stream().map(String::trim).collect(Collectors.toSet());

            Role role = roleManagement.createRole(tenantId, slug, displayName, description, scopes).block();
            if (role == null) {
                return "Failed to create role.";
            }
            return "✅ Created role **" + role.getDisplayName() + "** (`" + role.getSlug() + "`) with " +
                   scopes.size() + " permissions.";
        } catch (Exception e) {
            return "❌ Error creating role: " + e.getMessage();
        }
    }

    @Tool(description = "Update an existing custom role's display name, description, or permissions. " +
                        "System roles (admin, editor, viewer) cannot be modified. " +
                        "Use when the user wants to change a role's name or adjust its permissions.")
    public String updateRole(
            @ToolParam(description = "The role slug to update, e.g. 'custom:marketing-viewer'") String roleSlug,
            @ToolParam(description = "New display name (or null to keep current)") String displayName,
            @ToolParam(description = "New description (or null to keep current)") String description,
            @ToolParam(description = "New comma-separated scope slugs (or null to keep current)") String scopeSlugsCSV) {
        try {
            Set<String> scopes = null;
            if (scopeSlugsCSV != null && !scopeSlugsCSV.isBlank()) {
                scopes = new HashSet<>(List.of(scopeSlugsCSV.split(",")));
                scopes = scopes.stream().map(String::trim).collect(Collectors.toSet());
            }

            Role role = roleManagement.updateRole(roleSlug, displayName, description, scopes).block();
            if (role == null) {
                return "Role not found: " + roleSlug;
            }
            return "✅ Updated role **" + role.getDisplayName() + "** (`" + role.getSlug() + "`)";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @Tool(description = "Delete a custom role. System roles cannot be deleted. " +
                        "Use when the user wants to remove a custom role that is no longer needed. " +
                        "Always confirm with the user before deleting.")
    public String deleteRole(
            @ToolParam(description = "The role slug to delete, e.g. 'custom:marketing-viewer'") String roleSlug) {
        try {
            roleManagement.deleteRole(roleSlug).block();
            return "✅ Deleted role `" + roleSlug + "`";
        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }

    @Tool(description = "List all available permission scopes in the system, grouped by resource. " +
                        "Scopes follow 'resource:action' format. " +
                        "Use when the user asks what permissions exist, or needs help choosing scopes for a new role.")
    public String listScopes() {
        try {
            List<Scope> scopes = roleManagement.listScopes().collectList().block();
            if (scopes == null || scopes.isEmpty()) {
                return "No scopes found.";
            }

            // Group by resource
            var grouped = scopes.stream()
                .collect(Collectors.groupingBy(Scope::getResource, Collectors.toList()));

            StringBuilder sb = new StringBuilder("**Available Permissions (" + scopes.size() + "):**\n\n");
            grouped.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry -> {
                sb.append("### ").append(capitalize(entry.getKey())).append("\n");
                entry.getValue().forEach(scope ->
                    sb.append("- `").append(scope.getSlug()).append("` — ").append(scope.getDisplayName()).append("\n"));
                sb.append("\n");
            });
            return sb.toString();
        } catch (Exception e) {
            return "Error listing scopes: " + e.getMessage();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
