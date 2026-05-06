package com.synaptiq.branding.application.port.in;

import com.synaptiq.application.domain.model.Application;
import reactor.core.publisher.Mono;

public interface BrandingCommandUseCase {
    Mono<Application.Branding> updateBranding(String tenantId, String appId, Application.Branding branding);
    Mono<Application.Personalization> updatePersonalization(String tenantId, String appId, Application.Personalization personalization);
}
