package com.spectrayan.synaptiq.auth.infrastructure.security;

import com.spectrayan.synaptiq.auth.application.port.out.PathScopeMappingPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security-native authorization manager that resolves required scopes
 * from a DB-stored path→scope mapping.
 * <p>
 * Implements {@link ReactiveAuthorizationManager} — the standard Spring
 * Security 7 extension point for authorization decisions. Plugs directly
 * into {@code .authorizeExchange().access(this)}.
 * <p>
 * On startup, loads all path→scope mappings from MongoDB into an in-memory
 * cache. For each incoming request, matches the path + HTTP method against
 * the loaded patterns and checks if the user's authorities include the
 * required scope.
 * <p>
 * Supports:
 * <ul>
 *   <li>Exact scope match: user has {@code workflow:create}</li>
 *   <li>Resource wildcard: user has {@code workflow:*}</li>
 *   <li>Super-admin wildcard: user has {@code *}</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScopeAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final PathScopeMappingPort mappingPort;
    private final PathPatternParser patternParser = new PathPatternParser();

    /** Compiled route rules loaded from DB. */
    private volatile List<RouteRule> routeRules = List.of();

    /**
     * A compiled route rule: parsed PathPattern + HTTP method + required scope.
     */
    private record RouteRule(String httpMethod, PathPattern pattern, String requiredScope) {}

    /**
     * Loads path→scope mappings from MongoDB into memory.
     * Called on startup and can be called to refresh after mapping changes.
     */
    @PostConstruct
    public void loadMappings() {
        mappingPort.findAll()
            .collectList()
            .subscribe(mappings -> {
                this.routeRules = mappings.stream()
                    .map(m -> new RouteRule(
                        m.getHttpMethod().toUpperCase(),
                        patternParser.parse(m.getPathPattern()),
                        m.getRequiredScope()
                    ))
                    .toList();
                log.info("🔐 Loaded {} path→scope authorization rules", routeRules.size());
            }, err -> log.error("❌ Failed to load path→scope mappings", err));
    }

    /**
     * Reload mappings from DB — call this after path→scope mappings are updated.
     */
    public Mono<Void> refreshMappings() {
        return mappingPort.findAll()
            .collectList()
            .doOnNext(mappings -> {
                this.routeRules = mappings.stream()
                    .map(m -> new RouteRule(
                        m.getHttpMethod().toUpperCase(),
                        patternParser.parse(m.getPathPattern()),
                        m.getRequiredScope()
                    ))
                    .toList();
                log.info("🔐 Refreshed {} path→scope authorization rules", routeRules.size());
            })
            .then();
    }

    /**
     * Spring Security 7 authorization entry point.
     * Returns {@link AuthorizationResult} indicating whether access is granted.
     */
    @Override
    public Mono<AuthorizationResult> authorize(Mono<Authentication> authentication, AuthorizationContext context) {
        var exchange = context.getExchange();
        String method = exchange.getRequest().getMethod().name();
        PathContainer path = exchange.getRequest().getPath().pathWithinApplication();

        // Find the matching route rule
        String requiredScope = findRequiredScope(method, path);

        if (requiredScope == null) {
            // No mapping found — deny by default (fail-closed)
            log.warn("🚫 No scope mapping for {} {} — denying access", method, path.value());
            return Mono.just(new AuthorizationDecision(false));
        }

        return authentication
            .map(auth -> {
                if (!auth.isAuthenticated()) {
                    return (AuthorizationResult) new AuthorizationDecision(false);
                }

                Set<String> userScopes = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

                boolean granted = hasMatchingScope(userScopes, requiredScope);

                if (!granted) {
                    log.debug("🚫 Access denied for {} {} — required: {}, user has: {}",
                        method, path.value(), requiredScope, userScopes);
                }

                return (AuthorizationResult) new AuthorizationDecision(granted);
            })
            .defaultIfEmpty(new AuthorizationDecision(false));
    }

    /**
     * Finds the required scope for a given HTTP method and path.
     * Uses Spring's PathPattern matching (supports {id} variables, etc.)
     */
    private String findRequiredScope(String method, PathContainer path) {
        for (RouteRule rule : routeRules) {
            if (rule.httpMethod().equals(method) && rule.pattern().matches(path)) {
                return rule.requiredScope();
            }
        }
        return null;
    }

    /**
     * Checks if the user's scopes include the required scope.
     * Supports wildcards:
     * <ul>
     *   <li>{@code *} — matches everything (super-admin)</li>
     *   <li>{@code workflow:*} — matches any workflow action</li>
     *   <li>{@code workflow:create} — exact match</li>
     * </ul>
     */
    private boolean hasMatchingScope(Set<String> userScopes, String requiredScope) {
        // Full wildcard (super-admin)
        if (userScopes.contains("*")) {
            return true;
        }
        // Exact match
        if (userScopes.contains(requiredScope)) {
            return true;
        }
        // Resource wildcard (e.g. "workflow:*" matches "workflow:create")
        String resource = requiredScope.contains(":") ? requiredScope.split(":")[0] : requiredScope;
        return userScopes.contains(resource + ":*");
    }
}
