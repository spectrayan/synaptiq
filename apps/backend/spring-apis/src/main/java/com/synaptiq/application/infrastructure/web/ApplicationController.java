package com.synaptiq.application.infrastructure.web;

import com.synaptiq.application.application.port.in.ApplicationCrudUseCase;
import com.synaptiq.infrastructure.in.web.api.ApplicationsApi;
import com.synaptiq.infrastructure.in.web.dto.ApplicationResponse;
import com.synaptiq.infrastructure.in.web.dto.CreateApplicationRequest;
import com.synaptiq.infrastructure.in.web.dto.ListApplications200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * REST controller for the Application bounded context.
 * Implements the generated ApplicationsApi interface.
 */
@RestController
@RequiredArgsConstructor
public class ApplicationController implements ApplicationsApi {

    private final ApplicationCrudUseCase applicationCrud;
    private final ApplicationDtoMapper mapper;

    @Override
    public Mono<ResponseEntity<ApplicationResponse>> createApplication(
            Mono<CreateApplicationRequest> createApplicationRequest,
            String xTenantID,
            ServerWebExchange exchange) {
        return createApplicationRequest.flatMap(req -> {
            var command = new ApplicationCrudUseCase.CreateApplicationCommand(
                xTenantID,
                req.getName(),
                req.getSlug(),
                req.getDescription(),
                req.getIcon(),
                req.getIsDefault() != null && req.getIsDefault()
            );
            return applicationCrud.create(command)
                .map(mapper::toDto)
                .map(dto -> ResponseEntity.status(201).body(dto));
        });
    }

    @Override
    public Mono<ResponseEntity<ApplicationResponse>> getApplication(
            String appId,
            String xTenantID,
            ServerWebExchange exchange) {
        return applicationCrud.getByAppId(appId)
            .map(mapper::toDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<ListApplications200Response>> listApplications(
            String xTenantID,
            ServerWebExchange exchange) {
        return applicationCrud.listByTenantId(xTenantID)
            .map(mapper::toDto)
            .collectList()
            .map(apps -> ResponseEntity.ok(
                new ListApplications200Response().applications(apps)));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteApplication(
            String appId,
            String xTenantID,
            ServerWebExchange exchange) {
        return applicationCrud.delete(appId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
