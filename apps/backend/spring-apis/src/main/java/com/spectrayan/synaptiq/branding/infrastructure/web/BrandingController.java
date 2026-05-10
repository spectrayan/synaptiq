package com.spectrayan.synaptiq.branding.infrastructure.web;

import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.branding.application.port.in.BrandingCommandUseCase;
import com.spectrayan.synaptiq.branding.application.port.in.BrandingQueryUseCase;
import com.spectrayan.synaptiq.branding.application.port.in.ContrastCheckUseCase;
import com.spectrayan.synaptiq.infrastructure.in.web.api.BrandingApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.*;
import com.spectrayan.synaptiq.shared.infrastructure.web.AppIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BrandingController implements BrandingApi {
    private final BrandingQueryUseCase brandingQuery;
    private final BrandingCommandUseCase brandingCommand;
    private final ContrastCheckUseCase contrastCheck;
    private final AppIdResolver appIdResolver;

    @Override
    public Mono<ResponseEntity<BrandingResponse>> getBranding(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> brandingQuery.getBranding(xTenantID, appId))
            .map(b -> ResponseEntity.ok(toBrandingDto(b)));
    }

    @Override
    public Mono<ResponseEntity<BrandingResponse>> getPublicBranding(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> brandingQuery.getBranding(xTenantID, appId))
            .map(b -> ResponseEntity.ok(toBrandingDto(b)));
    }

    @Override
    public Mono<ResponseEntity<BrandingResponse>> updateBranding(Mono<BrandingResponse> body, String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID).flatMap(appId ->
            body.flatMap(b -> {
                var branding = com.spectrayan.synaptiq.application.domain.model.Application.Branding.builder()
                    .logoUrl(b.getLogoUrl()).primaryColor(b.getPrimaryColor())
                    .secondaryColor(b.getSecondaryColor()).backgroundStyle(b.getBackgroundStyle())
                    .headingFont(b.getHeadingFont()).bodyFont(b.getBodyFont())
                    .faviconUrl(b.getFaviconUrl()).pageTitle(b.getPageTitle())
                    .showPlatformBranding(b.getShowPlatformBranding() != null && b.getShowPlatformBranding())
                    .build();
                return brandingCommand.updateBranding(xTenantID, appId, branding)
                    .map(r -> ResponseEntity.ok(toBrandingDto(r)));
            }));
    }

    @Override
    public Mono<ResponseEntity<ThemeListResponse>> getThemes(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> brandingQuery.getThemes(xTenantID, appId))
            .map(themes -> ResponseEntity.ok(new ThemeListResponse()));
    }

    @Override
    public Mono<ResponseEntity<ContrastCheckResponse>> checkContrast(String fg, String bg, ServerWebExchange exchange) {
        return contrastCheck.checkContrast(fg, bg).map(r -> ResponseEntity.ok(new ContrastCheckResponse()
            .foreground(r.foreground()).background(r.background())
            .ratio(r.ratio()).aaNormal(r.aaNormal())
            .aaLarge(r.aaLarge()).aaaNormal(r.aaaNormal())));
    }

    @Override
    public Mono<ResponseEntity<PersonalizationResponse>> getPersonalization(String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID)
            .flatMap(appId -> brandingQuery.getPersonalization(xTenantID, appId))
            .map(p -> ResponseEntity.ok(toPersonalizationDto(p)));
    }

    @Override
    public Mono<ResponseEntity<PersonalizationResponse>> updatePersonalization(Mono<PersonalizationResponse> body, String xTenantID, ServerWebExchange exchange) {
        return appIdResolver.resolve(exchange, xTenantID).flatMap(appId ->
            body.flatMap(b -> {
                var personalization = com.spectrayan.synaptiq.application.domain.model.Application.Personalization.builder()
                    .allowThemeSwitch(b.getAllowThemeSwitch() != null && b.getAllowThemeSwitch())
                    .allowFontSwitch(b.getAllowFontSwitch() != null && b.getAllowFontSwitch())
                    .allowBubbleStyle(b.getAllowBubbleStyle() != null && b.getAllowBubbleStyle())
                    .build();
                return brandingCommand.updatePersonalization(xTenantID, appId, personalization)
                    .map(p -> ResponseEntity.ok(toPersonalizationDto(p)));
            }));
    }

    private BrandingResponse toBrandingDto(com.spectrayan.synaptiq.application.domain.model.Application.Branding b) {
        return new BrandingResponse()
            .logoUrl(b.getLogoUrl()).primaryColor(b.getPrimaryColor())
            .secondaryColor(b.getSecondaryColor()).backgroundStyle(b.getBackgroundStyle())
            .headingFont(b.getHeadingFont()).bodyFont(b.getBodyFont())
            .faviconUrl(b.getFaviconUrl()).pageTitle(b.getPageTitle())
            .showPlatformBranding(b.isShowPlatformBranding());
    }

    private PersonalizationResponse toPersonalizationDto(com.spectrayan.synaptiq.application.domain.model.Application.Personalization p) {
        return new PersonalizationResponse()
            .allowThemeSwitch(p.isAllowThemeSwitch())
            .allowFontSwitch(p.isAllowFontSwitch())
            .allowBubbleStyle(p.isAllowBubbleStyle());
    }
}
