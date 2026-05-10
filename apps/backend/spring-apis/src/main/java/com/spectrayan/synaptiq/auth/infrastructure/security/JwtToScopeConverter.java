package com.spectrayan.synaptiq.auth.infrastructure.security;

import com.spectrayan.synaptiq.auth.application.port.out.RolePersistencePort;
import com.spectrayan.synaptiq.auth.application.port.out.ScopePersistencePort;
import com.spectrayan.synaptiq.shared.config.CacheNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts a JWT into a Spring Security {@link JwtAuthenticationToken}
 * with scope-based {@link SimpleGrantedAuthority} entries.
 * <p>
 * Flow:
 * <ol>
 *   <li>Extract the role claim from JWT (configurable via {@code synaptiq.security.jwt.role-claim})</li>
 *   <li>Resolve role slug → scope slugs from MongoDB (cached)</li>
 *   <li>Map scope slugs to Spring {@code GrantedAuthority} entries</li>
 *   <li>Extract tenantId from JWT for downstream tenant isolation</li>
 * </ol>
 * <p>
 * Supports external auth servers by making claim paths configurable:
 * <ul>
 *   <li>Internal: {@code role} claim → "tenant:editor"</li>
 *   <li>Keycloak: set {@code synaptiq.security.jwt.role-claim=realm_access.roles[0]}</li>
 *   <li>Auth0: set {@code synaptiq.security.jwt.role-claim=https://app.example.com/role}</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtToScopeConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final RolePersistencePort rolePersistence;
    private final ScopePersistencePort scopePersistence;
    private final String roleClaim;
    private final String tenantClaim;

    public JwtToScopeConverter(
            RolePersistencePort rolePersistence,
            ScopePersistencePort scopePersistence,
            @Value("${synaptiq.security.jwt.role-claim:role}") String roleClaim,
            @Value("${synaptiq.security.jwt.tenant-claim:tenantId}") String tenantClaim) {
        this.rolePersistence = rolePersistence;
        this.scopePersistence = scopePersistence;
        this.roleClaim = roleClaim;
        this.tenantClaim = tenantClaim;
        log.info("🔑 JWT converter configured — role-claim: {}, tenant-claim: {}", roleClaim, tenantClaim);
    }

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        String roleSlug = extractClaim(jwt, roleClaim);
        String tenantId = extractClaim(jwt, tenantClaim);

        if (roleSlug == null || roleSlug.isBlank()) {
            log.warn("JWT for sub={} has no '{}' claim — granting zero authorities", jwt.getSubject(), roleClaim);
            return Mono.just(new JwtAuthenticationToken(jwt, Set.of()));
        }

        return resolveScopes(roleSlug)
            .map(scopes -> {
                var authorities = scopes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());

                // Add tenantId as a special authority for downstream tenant isolation
                if (tenantId != null && !tenantId.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("TENANT_" + tenantId));
                }

                return (AbstractAuthenticationToken) new JwtAuthenticationToken(jwt, authorities);
            })
            .defaultIfEmpty(new JwtAuthenticationToken(jwt, Set.of()));
    }

    /**
     * Resolves a role slug to its granted scopes.
     * Cached to avoid a DB round-trip on every request.
     */
    @Cacheable(value = CacheNames.ROLES, key = "#roleSlug")
    public Mono<Set<String>> resolveScopes(String roleSlug) {
        return rolePersistence.findBySlug(roleSlug)
            .flatMap(role -> {
                Set<String> scopes = role.getScopeSlugs();
                if (scopes != null && scopes.contains("*")) {
                    // Super-admin: expand wildcard to all known scopes + keep "*"
                    return scopePersistence.findAll()
                        .map(scope -> scope.getSlug())
                        .collect(Collectors.toSet())
                        .map(allScopes -> {
                            allScopes.add("*");
                            return allScopes;
                        });
                }
                return Mono.just(scopes != null ? scopes : Set.<String>of());
            })
            .defaultIfEmpty(Set.of());
    }

    /**
     * Extracts a claim value from the JWT, supporting dotted path notation
     * for nested claims (e.g. "realm_access.roles").
     */
    private String extractClaim(Jwt jwt, String claimPath) {
        if (claimPath == null || claimPath.isBlank()) return null;

        // Simple flat claim
        if (!claimPath.contains(".")) {
            return jwt.getClaimAsString(claimPath);
        }

        // Dotted path: navigate nested claims
        String[] parts = claimPath.split("\\.");
        Object current = jwt.getClaims();
        for (String part : parts) {
            if (current instanceof java.util.Map<?, ?> map) {
                current = map.get(part);
            } else {
                return null;
            }
        }
        return current != null ? current.toString() : null;
    }
}
