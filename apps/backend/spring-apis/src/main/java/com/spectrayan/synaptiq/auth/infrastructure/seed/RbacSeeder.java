package com.spectrayan.synaptiq.auth.infrastructure.seed;

import com.spectrayan.synaptiq.auth.application.port.out.PathScopeMappingPort;
import com.spectrayan.synaptiq.auth.application.port.out.RolePersistencePort;
import com.spectrayan.synaptiq.auth.application.port.out.ScopePersistencePort;
import com.spectrayan.synaptiq.auth.domain.model.PathScopeMapping;
import com.spectrayan.synaptiq.auth.domain.model.Role;
import com.spectrayan.synaptiq.auth.domain.model.Scope;
import com.spectrayan.synaptiq.auth.infrastructure.security.ScopeAuthorizationManager;
import com.spectrayan.synaptiq.infrastructure.in.web.api.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.spectrayan.synaptiq.auth.domain.model.Actions.*;
import static com.spectrayan.synaptiq.auth.domain.model.Resources.*;

/**
 * Seeds built-in scopes, system roles, and path→scope mappings on startup.
 * <p>
 * Uses:
 * <ul>
 *   <li>{@link com.spectrayan.synaptiq.auth.domain.model.Resources} for resource identifiers</li>
 *   <li>{@link com.spectrayan.synaptiq.auth.domain.model.Actions} for action identifiers</li>
 *   <li>Generated OpenAPI {@code PATH_*} constants for route patterns</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RbacSeeder {

    private final ScopePersistencePort scopePersistence;
    private final RolePersistencePort rolePersistence;
    private final PathScopeMappingPort pathMappingPort;
    private final ScopeAuthorizationManager authorizationManager;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        scopePersistence.count()
            .flatMap(count -> {
                if (count > 0) {
                    log.info("🔐 RBAC data already seeded ({} scopes) — loading mappings", count);
                    return authorizationManager.refreshMappings();
                }
                log.info("🔐 Seeding RBAC scopes, roles, and path→scope mappings...");
                return seedScopes()
                    .then(seedSystemRoles())
                    .then(seedPathMappings())
                    .then(authorizationManager.refreshMappings());
            })
            .subscribe(
                unused -> {},
                err -> log.error("❌ RBAC seeding failed", err),
                () -> log.info("✅ RBAC initialization complete")
            );
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Scopes — atomic permission units (resource:action)
    // ═══════════════════════════════════════════════════════════════════════

    private Mono<Void> seedScopes() {
        List<Scope> scopes = List.of(
            scope(APPLICATION, CREATE), scope(APPLICATION, READ),
            scope(APPLICATION, UPDATE), scope(APPLICATION, DELETE),
            scope(APPLICATION, LIST),

            scope(WORKFLOW, CREATE),    scope(WORKFLOW, READ),
            scope(WORKFLOW, UPDATE),    scope(WORKFLOW, DELETE),
            scope(WORKFLOW, LIST),      scope(WORKFLOW, EXECUTE),
            scope(WORKFLOW, SHARE),     scope(WORKFLOW, DUPLICATE),
            scope(WORKFLOW, GENERATE),

            scope(DATASOURCE, CREATE),  scope(DATASOURCE, READ),
            scope(DATASOURCE, UPDATE),  scope(DATASOURCE, DELETE),
            scope(DATASOURCE, LIST),

            scope(CHAT, SEND), scope(CHAT, READ), scope(CHAT, LIST),

            scope(BRANDING, READ), scope(BRANDING, UPDATE),

            scope(TENANT_CONFIG, READ), scope(TENANT_CONFIG, UPDATE),

            scope(TENANT, READ), scope(TENANT, UPDATE),
            scope(TENANT, MANAGE_USERS),

            scope(USER, READ), scope(USER, UPDATE_SELF),
            scope(USER, CHANGE_PASSWORD),

            scope(NOTIFICATION, READ), scope(NOTIFICATION, DISMISS),

            scope(ROLE, CREATE), scope(ROLE, READ),
            scope(ROLE, UPDATE), scope(ROLE, DELETE),
            scope(ROLE, LIST),

            scope(INTEGRATION, READ), scope(INTEGRATION, LIST),
            scope(INTEGRATION, CREATE), scope(INTEGRATION, EXECUTE),

            scope(ANALYTICS, READ),

            scope(SCHEMA_REGISTRY, READ), scope(SCHEMA_REGISTRY, QUERY)
        );

        return Flux.fromIterable(scopes).flatMap(scopePersistence::save).then();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // System Roles — named bundles of scopes (using resource:* wildcards)
    // ═══════════════════════════════════════════════════════════════════════

    private Mono<Void> seedSystemRoles() {
        List<Role> roles = List.of(
            systemRole("global:super-admin", "Super Admin",
                "Full system access across all tenants", "global",
                Set.of("*")),

            systemRole("tenant:admin", "Tenant Admin",
                "Full access within their tenant", "tenant",
                Set.of(
                    scopeWild(APPLICATION), scopeWild(WORKFLOW), scopeWild(DATASOURCE),
                    scopeWild(CHAT), scopeWild(BRANDING), scopeWild(TENANT_CONFIG),
                    scopeWild(TENANT), scopeWild(USER), scopeWild(NOTIFICATION),
                    scopeWild(ROLE), scopeWild(INTEGRATION), scopeWild(ANALYTICS),
                    scopeWild(SCHEMA_REGISTRY)
                )),

            systemRole("tenant:editor", "Editor",
                "Can manage workflows, apps, datasources, and chat", "tenant",
                Set.of(
                    scopeSlug(APPLICATION, CREATE), scopeSlug(APPLICATION, READ),
                    scopeSlug(APPLICATION, UPDATE), scopeSlug(APPLICATION, LIST),
                    scopeWild(WORKFLOW),
                    scopeSlug(DATASOURCE, CREATE), scopeSlug(DATASOURCE, READ),
                    scopeSlug(DATASOURCE, UPDATE), scopeSlug(DATASOURCE, LIST),
                    scopeWild(CHAT),
                    scopeSlug(BRANDING, READ),
                    scopeSlug(TENANT_CONFIG, READ),
                    scopeSlug(TENANT, READ),
                    scopeSlug(USER, READ), scopeSlug(USER, UPDATE_SELF),
                    scopeSlug(USER, CHANGE_PASSWORD),
                    scopeWild(NOTIFICATION),
                    scopeSlug(INTEGRATION, READ), scopeSlug(INTEGRATION, LIST),
                    scopeSlug(ANALYTICS, READ),
                    scopeSlug(SCHEMA_REGISTRY, READ)
                )),

            systemRole("tenant:viewer", "Viewer",
                "Read-only access plus chat", "tenant",
                Set.of(
                    scopeSlug(APPLICATION, READ), scopeSlug(APPLICATION, LIST),
                    scopeSlug(WORKFLOW, READ), scopeSlug(WORKFLOW, LIST),
                    scopeSlug(DATASOURCE, READ), scopeSlug(DATASOURCE, LIST),
                    scopeWild(CHAT),
                    scopeSlug(BRANDING, READ),
                    scopeSlug(TENANT_CONFIG, READ),
                    scopeSlug(TENANT, READ),
                    scopeSlug(USER, READ), scopeSlug(USER, UPDATE_SELF),
                    scopeSlug(USER, CHANGE_PASSWORD),
                    scopeWild(NOTIFICATION),
                    scopeSlug(INTEGRATION, READ), scopeSlug(INTEGRATION, LIST),
                    scopeSlug(ANALYTICS, READ),
                    scopeSlug(SCHEMA_REGISTRY, READ)
                ))
        );

        return Flux.fromIterable(roles)
            .filterWhen(role -> rolePersistence.existsBySlug(role.getSlug()).map(exists -> !exists))
            .flatMap(rolePersistence::save)
            .then();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Path → Scope Mappings — uses generated OpenAPI PATH_* constants
    // ═══════════════════════════════════════════════════════════════════════

    private Mono<Void> seedPathMappings() {
        List<PathScopeMapping> m = new ArrayList<>();

        // ── Auth (self-service) ── using AuthApi.PATH_*
        m.add(mapping("GET",  AuthApi.PATH_GET_CURRENT_USER,      scopeSlug(USER, READ)));
        m.add(mapping("PATCH", AuthApi.PATH_UPDATE_USER_ROLE,       scopeSlug(USER, UPDATE_SELF)));
        m.add(mapping("POST", AuthApi.PATH_CHANGE_PASSWORD,       scopeSlug(USER, CHANGE_PASSWORD)));

        // ── Tenants ── using TenantsApi.PATH_*
        m.add(mapping("POST", TenantsApi.PATH_CREATE_TENANT,      scopeSlug(TENANT, UPDATE)));
        m.add(mapping("GET",  TenantsApi.PATH_GET_TENANT,         scopeSlug(TENANT, READ)));
        m.add(mapping("PUT",  TenantsApi.PATH_UPDATE_TENANT,      scopeSlug(TENANT, UPDATE)));
        m.add(mapping("POST", TenantsApi.PATH_INVITE_ADMIN,       scopeSlug(TENANT, MANAGE_USERS)));
        m.add(mapping("GET",  TenantsApi.PATH_LIST_TENANT_ADMINS,   scopeSlug(TENANT, MANAGE_USERS)));

        // ── Applications ── using ApplicationsApi.PATH_*
        m.add(mapping("POST",   ApplicationsApi.PATH_CREATE_APPLICATION, scopeSlug(APPLICATION, CREATE)));
        m.add(mapping("GET",    ApplicationsApi.PATH_LIST_APPLICATIONS,  scopeSlug(APPLICATION, LIST)));
        m.add(mapping("GET",    ApplicationsApi.PATH_GET_APPLICATION,    scopeSlug(APPLICATION, READ)));
        m.add(mapping("DELETE", ApplicationsApi.PATH_DELETE_APPLICATION, scopeSlug(APPLICATION, DELETE)));

        // ── DataSources ── using DataSourcesApi.PATH_*
        m.add(mapping("POST",   DataSourcesApi.PATH_CREATE_DATA_SOURCE, scopeSlug(DATASOURCE, CREATE)));
        m.add(mapping("GET",    DataSourcesApi.PATH_LIST_DATA_SOURCES,  scopeSlug(DATASOURCE, LIST)));
        m.add(mapping("GET",    DataSourcesApi.PATH_GET_DATA_SOURCE,    scopeSlug(DATASOURCE, READ)));
        m.add(mapping("DELETE", DataSourcesApi.PATH_DELETE_DATA_SOURCE, scopeSlug(DATASOURCE, DELETE)));

        // (catalog module removed — datasource path mappings above cover data CRUD)

        // ── Chat ── using ChatApi.PATH_*
        m.add(mapping("POST",   ChatApi.PATH_POST_CHAT_MESSAGE,          scopeSlug(CHAT, SEND)));
        m.add(mapping("POST",   ChatApi.PATH_CREATE_SESSION,             scopeSlug(CHAT, SEND)));
        m.add(mapping("GET",    ChatApi.PATH_LIST_SESSIONS,              scopeSlug(CHAT, LIST)));
        m.add(mapping("GET",    ChatApi.PATH_DELETE_SESSION,             scopeSlug(CHAT, READ)));
        m.add(mapping("PUT",    ChatApi.PATH_UPDATE_SESSION,             scopeSlug(CHAT, SEND)));
        m.add(mapping("DELETE", ChatApi.PATH_DELETE_SESSION,             scopeSlug(CHAT, SEND)));
        m.add(mapping("GET",    ChatApi.PATH_GET_SESSION_HISTORY,        scopeSlug(CHAT, READ)));
        m.add(mapping("POST",   ChatApi.PATH_RESET_SESSION,             scopeSlug(CHAT, SEND)));

        // ── Actions ── using ActionsApi.PATH_*
        m.add(mapping("POST",   ActionsApi.PATH_EXECUTE_ACTION,       scopeSlug(CHAT, SEND)));
        m.add(mapping("GET",    ActionsApi.PATH_LIST_SAVED_ITEMS,     scopeSlug(CHAT, LIST)));
        m.add(mapping("DELETE", ActionsApi.PATH_REMOVE_SAVED_ITEM,    scopeSlug(CHAT, SEND)));

        // ── Workflows ── using WorkflowsApi.PATH_*
        m.add(mapping("POST",   WorkflowsApi.PATH_SAVE_WORKFLOW,          scopeSlug(WORKFLOW, CREATE)));
        m.add(mapping("GET",    WorkflowsApi.PATH_LIST_WORKFLOWS,         scopeSlug(WORKFLOW, LIST)));
        m.add(mapping("GET",    WorkflowsApi.PATH_GET_WORKFLOW,           scopeSlug(WORKFLOW, READ)));
        m.add(mapping("PATCH",  WorkflowsApi.PATH_UPDATE_WORKFLOW,        scopeSlug(WORKFLOW, UPDATE)));
        m.add(mapping("DELETE", WorkflowsApi.PATH_DELETE_WORKFLOW,        scopeSlug(WORKFLOW, DELETE)));
        m.add(mapping("POST",   WorkflowsApi.PATH_DUPLICATE_WORKFLOW,     scopeSlug(WORKFLOW, DUPLICATE)));
        m.add(mapping("POST",   WorkflowsApi.PATH_SHARE_WORKFLOW,         scopeSlug(WORKFLOW, SHARE)));
        m.add(mapping("GET",    WorkflowsApi.PATH_LIST_WORKFLOW_TEMPLATES, scopeSlug(WORKFLOW, LIST)));
        m.add(mapping("GET",    WorkflowsApi.PATH_LIST_WORKFLOW_TOOLS,    scopeSlug(WORKFLOW, READ)));
        m.add(mapping("GET",    WorkflowsApi.PATH_LIST_WORKFLOW_RUNS,     scopeSlug(WORKFLOW, READ)));
        m.add(mapping("GET",    WorkflowsApi.PATH_GET_WORKFLOW_RUN_DETAIL, scopeSlug(WORKFLOW, READ)));
        m.add(mapping("POST",   WorkflowsApi.PATH_EXECUTE_WORKFLOW,       scopeSlug(WORKFLOW, EXECUTE)));
        m.add(mapping("POST",   WorkflowsApi.PATH_GENERATE_WORKFLOW,      scopeSlug(WORKFLOW, GENERATE)));
        m.add(mapping("POST",   WorkflowsApi.PATH_REGENERATE_PROMPT,      scopeSlug(WORKFLOW, GENERATE)));

        // ── Analytics ── using AnalyticsApi.PATH_*
        m.add(mapping("GET", AnalyticsApi.PATH_GET_ANALYTICS_SUMMARY,  scopeSlug(ANALYTICS, READ)));
        m.add(mapping("GET", AnalyticsApi.PATH_GET_TOKEN_USAGE,        scopeSlug(ANALYTICS, READ)));
        m.add(mapping("GET", AnalyticsApi.PATH_GET_BILLING,            scopeSlug(ANALYTICS, READ)));
        m.add(mapping("GET", AnalyticsApi.PATH_GET_PLATFORM_ROLLUP,    scopeSlug(ANALYTICS, READ)));

        // ── Branding ── using BrandingApi.PATH_*
        m.add(mapping("GET", BrandingApi.PATH_GET_BRANDING,            scopeSlug(BRANDING, READ)));
        m.add(mapping("PUT", BrandingApi.PATH_UPDATE_BRANDING,         scopeSlug(BRANDING, UPDATE)));
        m.add(mapping("GET", BrandingApi.PATH_GET_THEMES,              scopeSlug(BRANDING, READ)));
        m.add(mapping("GET", BrandingApi.PATH_CHECK_CONTRAST,          scopeSlug(BRANDING, READ)));
        m.add(mapping("GET", BrandingApi.PATH_GET_PERSONALIZATION,     scopeSlug(BRANDING, READ)));
        m.add(mapping("PUT", BrandingApi.PATH_UPDATE_PERSONALIZATION,  scopeSlug(BRANDING, UPDATE)));

        // ── Tenant Config ── using ConfigApi.PATH_*
        m.add(mapping("GET", ConfigApi.PATH_GET_AI_CONFIG,              scopeSlug(TENANT_CONFIG, READ)));
        m.add(mapping("PUT", ConfigApi.PATH_UPDATE_AI_CONFIG,           scopeSlug(TENANT_CONFIG, UPDATE)));
        m.add(mapping("GET", ConfigApi.PATH_GET_GUARDRAILS_CONFIG,     scopeSlug(TENANT_CONFIG, READ)));
        m.add(mapping("PUT", ConfigApi.PATH_UPDATE_GUARDRAILS_CONFIG,  scopeSlug(TENANT_CONFIG, UPDATE)));
        m.add(mapping("GET", ConfigApi.PATH_GET_LLM_CONFIG,            scopeSlug(TENANT_CONFIG, READ)));
        m.add(mapping("PUT", ConfigApi.PATH_UPDATE_LLM_CONFIG,         scopeSlug(TENANT_CONFIG, UPDATE)));
        m.add(mapping("GET", ConfigApi.PATH_GET_COMPONENTS_CONFIG,     scopeSlug(TENANT_CONFIG, READ)));
        m.add(mapping("PUT", ConfigApi.PATH_UPDATE_COMPONENTS_CONFIG,  scopeSlug(TENANT_CONFIG, UPDATE)));
        m.add(mapping("GET", ConfigApi.PATH_GET_ACTIONS_CONFIG,         scopeSlug(TENANT_CONFIG, READ)));
        m.add(mapping("PUT", ConfigApi.PATH_UPDATE_ACTIONS_CONFIG,      scopeSlug(TENANT_CONFIG, UPDATE)));

        // ── Schema Registry ── using SchemaRegistryApi.PATH_*
        m.add(mapping("GET",  SchemaRegistryApi.PATH_LIST_COLLECTIONS,  scopeSlug(SCHEMA_REGISTRY, READ)));
        m.add(mapping("GET",  SchemaRegistryApi.PATH_INFER_SCHEMA,      scopeSlug(SCHEMA_REGISTRY, READ)));
        m.add(mapping("POST", SchemaRegistryApi.PATH_QUERY_COLLECTION,  scopeSlug(SCHEMA_REGISTRY, QUERY)));

        // ── Notifications ── using NotificationsApi.PATH_*
        m.add(mapping("GET",  NotificationsApi.PATH_LIST_NOTIFICATIONS,  scopeSlug(NOTIFICATION, READ)));
        m.add(mapping("PUT",  NotificationsApi.PATH_MARK_NOTIFICATION_AS_READ,  scopeSlug(NOTIFICATION, DISMISS)));
        m.add(mapping("GET",  NotificationsApi.PATH_GET_UNREAD_NOTIFICATION_COUNT, scopeSlug(NOTIFICATION, READ)));
        m.add(mapping("POST", NotificationsApi.PATH_MARK_ALL_NOTIFICATIONS_READ,   scopeSlug(NOTIFICATION, DISMISS)));

        // ── SSE (no generated constant — internal endpoint) ──
        m.add(mapping("GET", "/api/v1/sse/**",                          scopeSlug(NOTIFICATION, READ)));

        // ── RBAC ── using RolesApi.PATH_*
        m.add(mapping("GET",    RolesApi.PATH_LIST_ROLES,   scopeSlug(ROLE, LIST)));
        m.add(mapping("POST",   RolesApi.PATH_CREATE_ROLE,  scopeSlug(ROLE, CREATE)));
        m.add(mapping("GET",    RolesApi.PATH_GET_ROLE,     scopeSlug(ROLE, READ)));
        m.add(mapping("PUT",    RolesApi.PATH_UPDATE_ROLE,  scopeSlug(ROLE, UPDATE)));
        m.add(mapping("DELETE", RolesApi.PATH_DELETE_ROLE,  scopeSlug(ROLE, DELETE)));
        m.add(mapping("GET",    RolesApi.PATH_LIST_SCOPES,  scopeSlug(ROLE, LIST)));

        return Flux.fromIterable(m).flatMap(pathMappingPort::save).then();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════════════════════

    /** Builds scope slug: "resource:action" */
    private static String scopeSlug(String resource, String action) {
        return resource + ":" + action;
    }

    /** Builds wildcard scope: "resource:*" */
    private static String scopeWild(String resource) {
        return resource + ":" + ALL;
    }

    private static Scope scope(String resource, String action) {
        String slug = scopeSlug(resource, action);
        String name = capitalize(action) + " " + capitalize(resource.replace("-", " "));
        return Scope.builder()
            .slug(slug).displayName(name)
            .description("Allows: " + name)
            .resource(resource).action(action)
            .build();
    }

    private static Role systemRole(String slug, String displayName, String description,
                                    String roleType, Set<String> scopeSlugs) {
        return Role.builder()
            .slug(slug).displayName(displayName).description(description)
            .systemRole(true).roleType(roleType).scopeSlugs(scopeSlugs)
            .build();
    }

    private static PathScopeMapping mapping(String method, String path, String scope) {
        return PathScopeMapping.builder()
            .httpMethod(method).pathPattern(path).requiredScope(scope)
            .build();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
