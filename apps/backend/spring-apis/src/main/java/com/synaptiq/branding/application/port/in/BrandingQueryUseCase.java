package com.synaptiq.branding.application.port.in;

import com.synaptiq.application.domain.model.Application;
import reactor.core.publisher.Mono;
import java.util.List;

public interface BrandingQueryUseCase {
    Mono<Application.Branding> getBranding(String tenantId, String appId);
    Mono<List<Application.ThemePreset>> getThemes(String tenantId, String appId);
    Mono<Application.Personalization> getPersonalization(String tenantId, String appId);
}
