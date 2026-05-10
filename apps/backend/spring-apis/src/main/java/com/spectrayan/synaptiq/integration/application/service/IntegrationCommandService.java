package com.spectrayan.synaptiq.integration.application.service;

import com.spectrayan.synaptiq.integration.application.port.in.IntegrationCommandUseCase;
import com.spectrayan.synaptiq.integration.core.RouteLifecycleService;
import com.spectrayan.synaptiq.integration.model.*;
import com.spectrayan.synaptiq.integration.spi.RouteConfigProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Application service implementing integration write operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntegrationCommandService implements IntegrationCommandUseCase {

    private final RouteConfigProvider routeConfigProvider;
    private final RouteLifecycleService routeLifecycleService;

    @Override
    public Mono<RouteConfig> create(CreateIntegrationCommand command) {
        RouteConfig config = RouteConfig.builder()
                .routeConfigId(UUID.randomUUID().toString())
                .tenantId(command.tenantId())
                .name(command.name())
                .description(command.description())
                .connectorType(command.connectorType())
                .templateId(command.templateId())
                .parameters(command.parameters() != null ? command.parameters() : java.util.Map.of())
                .credentialRef(command.credentialRef())
                .status(RouteStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return routeConfigProvider.save(config)
                .doOnSuccess(c -> log.info("Created integration '{}' for tenant '{}'",
                        c.getName(), c.getTenantId()));
    }

    @Override
    public Mono<RouteConfig> activate(String routeConfigId) {
        return routeLifecycleService.activateRoute(routeConfigId);
    }

    @Override
    public Mono<RouteConfig> deactivate(String routeConfigId) {
        return routeLifecycleService.deactivateRoute(routeConfigId);
    }

    @Override
    public Mono<ConnectionTestResult> testConnection(String routeConfigId) {
        return routeLifecycleService.testConnection(routeConfigId);
    }

    @Override
    public Mono<Void> delete(String routeConfigId) {
        return routeLifecycleService.deactivateRoute(routeConfigId)
                .onErrorResume(e -> Mono.empty())
                .then(routeConfigProvider.deleteByRouteConfigId(routeConfigId));
    }
}
