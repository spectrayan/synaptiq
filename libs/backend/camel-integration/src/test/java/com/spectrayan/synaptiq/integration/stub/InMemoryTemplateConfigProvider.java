package com.spectrayan.synaptiq.integration.stub;

import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import com.spectrayan.synaptiq.integration.spi.TemplateConfigProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory {@link TemplateConfigProvider} for testing.
 */
public class InMemoryTemplateConfigProvider implements TemplateConfigProvider {

    private final Map<String, TemplateDescriptor> store = new ConcurrentHashMap<>();

    public void clear() {
        store.clear();
    }

    public int size() {
        return store.size();
    }

    @Override
    public Flux<TemplateDescriptor> findAllAccessibleByTenant(String tenantId) {
        return Flux.fromIterable(store.values())
                .filter(t -> t.getTenantId() == null || t.getTenantId().equals(tenantId));
    }

    @Override
    public Flux<TemplateDescriptor> findAll() {
        return Flux.fromIterable(store.values());
    }

    @Override
    public Mono<TemplateDescriptor> findByTemplateId(String templateId) {
        TemplateDescriptor t = store.get(templateId);
        return t != null ? Mono.just(t) : Mono.empty();
    }

    @Override
    public Mono<TemplateDescriptor> save(TemplateDescriptor template) {
        store.put(template.getTemplateId(), template);
        return Mono.just(template);
    }

    @Override
    public Mono<Void> deleteByTemplateId(String templateId) {
        store.remove(templateId);
        return Mono.empty();
    }
}
