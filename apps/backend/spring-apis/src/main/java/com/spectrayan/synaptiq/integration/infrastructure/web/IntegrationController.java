package com.spectrayan.synaptiq.integration.infrastructure.web;

import com.spectrayan.synaptiq.integration.application.port.in.IntegrationCommandUseCase;
import com.spectrayan.synaptiq.integration.application.port.in.IntegrationCommandUseCase.CreateIntegrationCommand;
import com.spectrayan.synaptiq.integration.application.port.in.IntegrationQueryUseCase;
import com.spectrayan.synaptiq.integration.model.ConnectionTestResult;
import com.spectrayan.synaptiq.integration.model.RouteConfig;
import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * REST controller for the integration module.
 */
@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationCommandUseCase commandUseCase;
    private final IntegrationQueryUseCase queryUseCase;

    // ═══════════════════════════════════════════════════════════════
    //  Templates (platform-wide registry)
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/templates")
    public List<TemplateDescriptor> listTemplates() {
        return queryUseCase.listTemplates();
    }

    // ═══════════════════════════════════════════════════════════════
    //  Integrations (per-tenant)
    // ═══════════════════════════════════════════════════════════════

    @GetMapping
    public Flux<RouteConfig> listIntegrations(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return queryUseCase.listByTenantId(tenantId);
    }

    @GetMapping("/{routeConfigId}")
    public Mono<RouteConfig> getIntegration(@PathVariable String routeConfigId) {
        return queryUseCase.getByRouteConfigId(routeConfigId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RouteConfig> createIntegration(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody CreateIntegrationRequest request) {
        return commandUseCase.create(new CreateIntegrationCommand(
                tenantId,
                request.name(),
                request.description(),
                request.connectorType(),
                request.templateId(),
                request.parameters(),
                request.credentialRef()
        ));
    }

    @PostMapping("/{routeConfigId}/activate")
    public Mono<RouteConfig> activateIntegration(@PathVariable String routeConfigId) {
        return commandUseCase.activate(routeConfigId);
    }

    @PostMapping("/{routeConfigId}/deactivate")
    public Mono<RouteConfig> deactivateIntegration(@PathVariable String routeConfigId) {
        return commandUseCase.deactivate(routeConfigId);
    }

    @PostMapping("/{routeConfigId}/test")
    public Mono<ConnectionTestResult> testIntegration(@PathVariable String routeConfigId) {
        return commandUseCase.testConnection(routeConfigId);
    }

    @DeleteMapping("/{routeConfigId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteIntegration(@PathVariable String routeConfigId) {
        return commandUseCase.delete(routeConfigId);
    }

    // ── Request DTO ─────────────────────────────────────────────

    record CreateIntegrationRequest(
            String name,
            String description,
            String connectorType,
            String templateId,
            Map<String, String> parameters,
            String credentialRef
    ) {}
}
