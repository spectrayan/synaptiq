package com.synaptiq.tenant.infrastructure.web;

import com.synaptiq.infrastructure.in.web.api.TenantsApi;
import com.synaptiq.infrastructure.in.web.dto.*;
import com.synaptiq.tenant.application.port.in.CreateTenantUseCase;
import com.synaptiq.tenant.application.port.in.GetTenantUseCase;
import com.synaptiq.tenant.application.port.in.UpdateTenantUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TenantController implements TenantsApi {
    private final CreateTenantUseCase createTenantUseCase;
    private final GetTenantUseCase getTenantUseCase;
    private final UpdateTenantUseCase updateTenantUseCase;
    private final TenantDtoMapper mapper;

    @Override
    public Mono<ResponseEntity<TenantResponse>> createTenant(
            Mono<CreateTenantRequest> req, ServerWebExchange exchange) {
        return req.flatMap(r -> createTenantUseCase.createTenant(
                new CreateTenantUseCase.CreateTenantCommand(
                    r.getTenantId(), r.getName(), r.getSlug(), r.getCatalogLabel(),
                    r.getAccessMode() != null ? r.getAccessMode().getValue() : "PUBLIC"))
            .map(mapper::toDto)
            .map(d -> ResponseEntity.status(201).body(d)));
    }

    @Override
    public Mono<ResponseEntity<TenantResponse>> getTenant(String tenantId, ServerWebExchange exchange) {
        return getTenantUseCase.getByTenantId(tenantId)
            .map(mapper::toDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<TenantResponse>> updateTenant(String tenantId,
            Mono<UpdateTenantRequest> requestBody, ServerWebExchange exchange) {
        return requestBody.flatMap(body -> {
            var command = new UpdateTenantUseCase.UpdateTenantCommand(
                body.getName(),
                body.getSlug(),
                body.getCatalogLabel(),
                body.getAccessMode() != null ? body.getAccessMode().getValue() : null,
                body.getPlanTier() != null ? body.getPlanTier().getValue() : null,
                body.getDbConnectionUri()
            );
            return updateTenantUseCase.updateTenant(tenantId, command)
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
        });
    }

    @Override
    public Mono<ResponseEntity<Flux<TenantAdminResponse>>> listTenantAdmins(String tenantId, ServerWebExchange exchange) {
        return getTenantUseCase.getByTenantId(tenantId)
            .map(t -> {
                Flux<TenantAdminResponse> admins = Flux.fromIterable(t.getAdmins()).map(a -> new TenantAdminResponse()
                    .uid(a.getUid()).email(a.getEmail())
                    .role(a.getRole() != null ? AdminRole.fromValue(a.getRole().name()) : null)
                    .accepted(a.isAccepted()));
                return ResponseEntity.ok(admins);
            });
    }

    @Override
    public Mono<ResponseEntity<TenantAdminResponse>> inviteAdmin(String tenantId,
            Mono<InviteAdminRequest> req, ServerWebExchange exchange) {
        return req.flatMap(r -> Mono.just(ResponseEntity.status(201)
            .body(new TenantAdminResponse()
                .email(r.getEmail())
                .role(r.getRole())
                .accepted(false))));
    }
}
