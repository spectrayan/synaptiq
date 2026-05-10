package com.spectrayan.synaptiq.application.infrastructure.web;

import com.spectrayan.synaptiq.application.application.port.in.ApplicationCommandUseCase;
import com.spectrayan.synaptiq.application.application.port.in.ApplicationQueryUseCase;
import com.spectrayan.synaptiq.infrastructure.in.web.api.ApplicationsApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ApplicationResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.CreateApplicationRequest;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ListApplications200Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * REST controller for the Application bounded context.
 * Implements the generated ApplicationsApi interface.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ApplicationController implements ApplicationsApi {

    private final ApplicationCommandUseCase applicationCommand;
    private final ApplicationQueryUseCase applicationQuery;
    private final ApplicationDtoMapper mapper;

    @Override
    public Mono<ResponseEntity<ApplicationResponse>> createApplication(
            Mono<CreateApplicationRequest> createApplicationRequest,
            String xTenantID,
            ServerWebExchange exchange) {
        return createApplicationRequest.flatMap(req -> {
            var command = new ApplicationCommandUseCase.CreateApplicationCommand(
                xTenantID,
                req.getName(),
                req.getSlug(),
                req.getDescription(),
                req.getIcon(),
                req.getIsDefault() != null && req.getIsDefault()
            );
            return applicationCommand.create(command)
                .map(mapper::toDto)
                .map(dto -> ResponseEntity.status(201).body(dto));
        });
    }

    @Override
    public Mono<ResponseEntity<ApplicationResponse>> getApplication(
            String appId,
            String xTenantID,
            ServerWebExchange exchange) {
        return applicationQuery.getByAppId(appId)
            .map(mapper::toDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<ListApplications200Response>> listApplications(
            String xTenantID,
            ServerWebExchange exchange) {
        return applicationQuery.listByTenantId(xTenantID)
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
        return applicationCommand.delete(appId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
