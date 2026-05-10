package com.spectrayan.synaptiq.tenantconfig.application.service;

import com.spectrayan.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.shared.config.CacheNames;
import com.spectrayan.synaptiq.shared.exception.ResourceNotFoundException;
import com.spectrayan.synaptiq.tenantconfig.application.port.in.TenantConfigCommandUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Updates config facets on the resolved Application entity.
 * Persists changes via ApplicationPersistencePort — no Map&lt;String,Object&gt; bridge.
 * <p>
 * All update methods evict the relevant caches so subsequent reads
 * fetch fresh data from the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantConfigCommandService implements TenantConfigCommandUseCase {
    private final ApplicationPersistencePort applicationPersistence;

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS, allEntries = true),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, allEntries = true)
    })
    public Mono<Application.AIPersona> updateAiConfig(String tid, String appId, Application.AIPersona persona) {
        log.info("Updating AI config for tenant='{}', appId='{}'", tid, appId);
        return resolveApp(tid, appId).flatMap(app -> {
            app.setAiPersona(persona);
            return applicationPersistence.save(app).map(Application::getAiPersona);
        });
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS, allEntries = true),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, allEntries = true)
    })
    public Mono<Application.Guardrails> updateGuardrails(String tid, String appId, Application.Guardrails guardrails) {
        log.info("Updating guardrails for tenant='{}', appId='{}'", tid, appId);
        return resolveApp(tid, appId).flatMap(app -> {
            app.setGuardrails(guardrails);
            return applicationPersistence.save(app).map(Application::getGuardrails);
        });
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS, allEntries = true),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, allEntries = true)
    })
    public Mono<Application.LlmProviderConfig> updateLlmProvider(String tid, String appId, Application.LlmProviderConfig provider) {
        log.info("Updating LLM provider for tenant='{}', appId='{}'", tid, appId);
        return resolveApp(tid, appId).flatMap(app -> {
            app.setLlmOverride(provider);
            return applicationPersistence.save(app).map(Application::getLlmOverride);
        });
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS, allEntries = true),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, allEntries = true)
    })
    public Mono<Application.ComponentSet> updateComponents(String tid, String appId, Application.ComponentSet components) {
        log.info("Updating components for tenant='{}', appId='{}'", tid, appId);
        return resolveApp(tid, appId).flatMap(app -> {
            app.setComponents(components);
            return applicationPersistence.save(app).map(Application::getComponents);
        });
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = CacheNames.APPLICATIONS, allEntries = true),
        @CacheEvict(value = CacheNames.DEFAULT_APPLICATION, allEntries = true)
    })
    public Mono<Application.ActionsConfig> updateActions(String tid, String appId, Application.ActionsConfig actions) {
        log.info("Updating actions config for tenant='{}', appId='{}'", tid, appId);
        return resolveApp(tid, appId).flatMap(app -> {
            app.setActions(actions);
            return applicationPersistence.save(app).map(Application::getActions);
        });
    }

    private Mono<Application> resolveApp(String tenantId, String appId) {
        if (appId != null && !appId.isBlank()) {
            return applicationPersistence.findByAppId(appId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                    "Application '" + appId + "' not found")));
        }
        return applicationPersistence.findDefaultByTenantId(tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                "No default application found for tenant '" + tenantId + "'")));
    }
}
