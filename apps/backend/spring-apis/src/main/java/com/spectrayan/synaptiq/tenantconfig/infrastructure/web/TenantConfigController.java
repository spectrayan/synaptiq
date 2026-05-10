package com.spectrayan.synaptiq.tenantconfig.infrastructure.web;

import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.infrastructure.in.web.api.ConfigApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.*;
import com.spectrayan.synaptiq.shared.infrastructure.web.AppIdResolver;
import com.spectrayan.synaptiq.tenantconfig.application.port.in.TenantConfigCommandUseCase;
import com.spectrayan.synaptiq.tenantconfig.application.port.in.TenantConfigQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TenantConfigController implements ConfigApi {
    private final TenantConfigQueryUseCase configQuery;
    private final TenantConfigCommandUseCase configCommand;
    private final AppIdResolver appIdResolver;

    @Override
    public Mono<ResponseEntity<AiPersonaResponse>> getAiConfig(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> configQuery.getAiConfig(xTenantID, appId))
            .map(p -> ResponseEntity.ok(toAiPersonaDto(p)));
    }

    @Override
    public Mono<ResponseEntity<AiPersonaResponse>> updateAiConfig(Mono<AiPersonaResponse> req, String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID).flatMap(appId ->
            req.flatMap(r -> {
                var persona = Application.AIPersona.builder()
                    .displayName(r.getDisplayName()).tone(r.getTone())
                    .customInstruction(r.getCustomInstruction())
                    .welcomeMessage(r.getWelcomeMessage())
                    .build();
                return configCommand.updateAiConfig(xTenantID, appId, persona)
                    .map(p -> ResponseEntity.ok(toAiPersonaDto(p)));
            }));
    }

    @Override
    public Mono<ResponseEntity<GuardrailsResponse>> getGuardrailsConfig(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> configQuery.getGuardrails(xTenantID, appId))
            .map(g -> ResponseEntity.ok(toGuardrailsDto(g)));
    }

    @Override
    public Mono<ResponseEntity<GuardrailsResponse>> updateGuardrailsConfig(Mono<GuardrailsResponse> req, String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID).flatMap(appId ->
            req.flatMap(r -> {
                var guardrails = Application.Guardrails.builder()
                    .outOfScopeMessage(r.getOutOfScopeMessage())
                    .recommendationMode(r.getRecommendationMode() != null && r.getRecommendationMode())
                    .language(r.getLanguage())
                    .build();
                return configCommand.updateGuardrails(xTenantID, appId, guardrails)
                    .map(g -> ResponseEntity.ok(toGuardrailsDto(g)));
            }));
    }

    @Override
    public Mono<ResponseEntity<LlmProviderResponse>> getLlmConfig(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> configQuery.getLlmProvider(xTenantID, appId))
            .map(l -> ResponseEntity.ok(toLlmDto(l)));
    }

    @Override
    public Mono<ResponseEntity<LlmProviderResponse>> updateLlmConfig(Mono<LlmProviderResponse> req, String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID).flatMap(appId ->
            req.flatMap(r -> {
                var provider = Application.LlmProviderConfig.builder()
                    .provider(r.getProvider()).modelId(r.getModelId())
                    .isByok(r.getIsByok() != null && r.getIsByok())
                    .build();
                return configCommand.updateLlmProvider(xTenantID, appId, provider)
                    .map(l -> ResponseEntity.ok(toLlmDto(l)));
            }));
    }

    @Override
    public Mono<ResponseEntity<ComponentsToggleResponse>> getComponentsConfig(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> configQuery.getComponents(xTenantID, appId))
            .map(c -> ResponseEntity.ok(toComponentsDto(c)));
    }

    @Override
    public Mono<ResponseEntity<ComponentsToggleResponse>> updateComponentsConfig(Mono<ComponentsToggleResponse> req, String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID).flatMap(appId ->
            req.flatMap(r -> {
                var components = Application.ComponentSet.builder()
                    .itemCard(r.getItemCard() != null && r.getItemCard())
                    .itemGrid(r.getItemGrid() != null && r.getItemGrid())
                    .itemDetail(r.getItemDetail() != null && r.getItemDetail())
                    .comparisonTable(r.getComparisonTable() != null && r.getComparisonTable())
                    .filterSummary(r.getFilterSummary() != null && r.getFilterSummary())
                    .resultCount(r.getResultCount() != null && r.getResultCount())
                    .emptyState(r.getEmptyState() != null && r.getEmptyState())
                    .actionConfirm(r.getActionConfirm() != null && r.getActionConfirm())
                    .infoBanner(r.getInfoBanner() != null && r.getInfoBanner())
                    .build();
                return configCommand.updateComponents(xTenantID, appId, components)
                    .map(c -> ResponseEntity.ok(toComponentsDto(c)));
            }));
    }

    @Override
    public Mono<ResponseEntity<ActionsConfigResponse>> getActionsConfig(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> configQuery.getActions(xTenantID, appId))
            .map(a -> ResponseEntity.ok(toActionsDto(a)));
    }

    @Override
    public Mono<ResponseEntity<ActionsConfigResponse>> updateActionsConfig(Mono<ActionsConfigResponse> req, String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID).flatMap(appId ->
            req.flatMap(r -> {
                var actions = Application.ActionsConfig.builder()
                    .enquiryWebhookUrl(r.getEnquiryWebhookUrl())
                    .enquiryEmail(r.getEnquiryEmail())
                    .build();
                return configCommand.updateActions(xTenantID, appId, actions)
                    .map(a -> ResponseEntity.ok(toActionsDto(a)));
            }));
    }

    // ── Domain → DTO mappers (at infrastructure boundary only) ──────

    private AiPersonaResponse toAiPersonaDto(Application.AIPersona p) {
        return new AiPersonaResponse()
            .displayName(p.getDisplayName()).tone(p.getTone())
            .customInstruction(p.getCustomInstruction())
            .welcomeMessage(p.getWelcomeMessage());
    }

    private GuardrailsResponse toGuardrailsDto(Application.Guardrails g) {
        return new GuardrailsResponse()
            .outOfScopeMessage(g.getOutOfScopeMessage())
            .recommendationMode(g.isRecommendationMode())
            .language(g.getLanguage());
    }

    private LlmProviderResponse toLlmDto(Application.LlmProviderConfig l) {
        return new LlmProviderResponse()
            .provider(l.getProvider()).modelId(l.getModelId())
            .isByok(l.isByok());
    }

    private ComponentsToggleResponse toComponentsDto(Application.ComponentSet c) {
        return new ComponentsToggleResponse()
            .itemCard(c.isItemCard()).itemGrid(c.isItemGrid())
            .itemDetail(c.isItemDetail()).comparisonTable(c.isComparisonTable())
            .filterSummary(c.isFilterSummary()).resultCount(c.isResultCount())
            .emptyState(c.isEmptyState()).actionConfirm(c.isActionConfirm())
            .infoBanner(c.isInfoBanner());
    }

    private ActionsConfigResponse toActionsDto(Application.ActionsConfig a) {
        return new ActionsConfigResponse()
            .enquiryWebhookUrl(a.getEnquiryWebhookUrl())
            .enquiryEmail(a.getEnquiryEmail());
    }
}
