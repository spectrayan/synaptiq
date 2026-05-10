package com.spectrayan.synaptiq.auth.infrastructure.web;

import com.spectrayan.synaptiq.auth.application.port.in.RoleManagementUseCase;
import com.spectrayan.synaptiq.auth.domain.model.Role;
import com.spectrayan.synaptiq.auth.domain.model.Scope;
import com.spectrayan.synaptiq.infrastructure.in.web.api.RolesApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.CreateRoleRequest;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ListRoles200Response;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ListScopes200Response;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.RoleResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ScopeResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.UpdateCustomRoleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

/**
 * REST controller for RBAC role and scope management.
 * Implements the OpenAPI-generated {@link RolesApi} interface.
 */
@RestController
@RequiredArgsConstructor
public class RoleController implements RolesApi {

    private final RoleManagementUseCase roleManagement;

    // Generated signature: createRole(Mono<CreateRoleRequest>, @Nullable String xTenantID, ServerWebExchange)
    @Override
    public Mono<ResponseEntity<RoleResponse>> createRole(
            Mono<CreateRoleRequest> createRoleRequest,
            @Nullable String xTenantID,
            ServerWebExchange exchange) {
        return createRoleRequest.flatMap(req ->
            roleManagement.createRole(
                xTenantID,
                req.getSlug(),
                req.getDisplayName(),
                req.getDescription(),
                new HashSet<>(req.getScopeSlugs())
            ).map(role -> ResponseEntity.status(HttpStatus.CREATED).body(toResponse(role)))
        );
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteRole(
            String roleSlug,
            ServerWebExchange exchange) {
        return roleManagement.deleteRole(roleSlug)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<RoleResponse>> getRole(
            String roleSlug,
            ServerWebExchange exchange) {
        return roleManagement.getRole(roleSlug)
            .map(role -> ResponseEntity.ok(toResponse(role)));
    }

    // Generated signature: updateRole(String roleSlug, Mono<UpdateCustomRoleRequest>, ServerWebExchange)
    @Override
    public Mono<ResponseEntity<RoleResponse>> updateRole(
            String roleSlug,
            Mono<UpdateCustomRoleRequest> updateCustomRoleRequest,
            ServerWebExchange exchange) {
        return updateCustomRoleRequest.flatMap(req ->
            roleManagement.updateRole(
                roleSlug,
                req.getDisplayName(),
                req.getDescription(),
                req.getScopeSlugs() != null ? new HashSet<>(req.getScopeSlugs()) : null
            ).map(role -> ResponseEntity.ok(toResponse(role)))
        );
    }

    @Override
    public Mono<ResponseEntity<ListRoles200Response>> listRoles(
            @Nullable String xTenantID,
            ServerWebExchange exchange) {
        return roleManagement.listRoles(xTenantID)
            .map(this::toResponse)
            .collectList()
            .map(roles -> {
                var resp = new ListRoles200Response();
                resp.setRoles(roles);
                return ResponseEntity.ok(resp);
            });
    }

    @Override
    public Mono<ResponseEntity<ListScopes200Response>> listScopes(
            ServerWebExchange exchange) {
        return roleManagement.listScopes()
            .map(this::toScopeResponse)
            .collectList()
            .map(scopes -> {
                var resp = new ListScopes200Response();
                resp.setScopes(scopes);
                return ResponseEntity.ok(resp);
            });
    }

    // ── Mappers ──────────────────────────────────────────────────────────

    private RoleResponse toResponse(Role role) {
        var r = new RoleResponse();
        r.setId(role.getId());
        r.setSlug(role.getSlug());
        r.setTenantId(role.getTenantId());
        r.setDisplayName(role.getDisplayName());
        r.setDescription(role.getDescription());
        r.setSystemRole(role.isSystemRole());
        r.setRoleType(role.getRoleType() != null
            ? RoleResponse.RoleTypeEnum.fromValue(role.getRoleType())
            : null);
        r.setScopeSlugs(role.getScopeSlugs() != null
            ? List.copyOf(role.getScopeSlugs())
            : List.of());
        return r;
    }

    private ScopeResponse toScopeResponse(Scope scope) {
        var s = new ScopeResponse();
        s.setSlug(scope.getSlug());
        s.setDisplayName(scope.getDisplayName());
        s.setDescription(scope.getDescription());
        s.setResource(scope.getResource());
        s.setAction(scope.getAction());
        return s;
    }
}
