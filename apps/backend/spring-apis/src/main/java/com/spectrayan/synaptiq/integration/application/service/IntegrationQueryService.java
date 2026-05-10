package com.spectrayan.synaptiq.integration.application.service;

import com.spectrayan.synaptiq.integration.application.port.in.IntegrationQueryUseCase;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import com.spectrayan.synaptiq.integration.spi.RouteConfigProvider;
import com.spectrayan.synaptiq.integration.template.TemplateRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Application service implementing integration read operations.
 */
@Service
@RequiredArgsConstructor
public class IntegrationQueryService implements IntegrationQueryUseCase {

    private final RouteConfigProvider routeConfigProvider;
    private final TemplateRegistry templateRegistry;

    @Override
    public Flux<RouteConfig> listByTenantId(String tenantId) {
        return routeConfigProvider.findAllByTenantId(tenantId);
    }

    @Override
    public Mono<RouteConfig> getByRouteConfigId(String routeConfigId) {
        return routeConfigProvider.findByRouteConfigId(routeConfigId);
    }

    @Override
    public List<TemplateDescriptor> listTemplates() {
        return templateRegistry.listTemplates();
    }
}
