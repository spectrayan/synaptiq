package com.spectrayan.synaptiq.tenant.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.tenant.application.port.out.TenantPersistencePort;
import com.spectrayan.synaptiq.tenant.domain.model.Tenant;
import com.spectrayan.synaptiq.tenant.infrastructure.persistence.mongo.mapper.TenantPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adapter implementing the domain's {@link TenantPersistencePort}.
 * Converts between domain models and MongoDB documents.
 */
@Component
@RequiredArgsConstructor
public class TenantMongoAdapter implements TenantPersistencePort {

    private final TenantReactiveMongoRepository mongoRepository;
    private final TenantPersistenceMapper mapper;

    @Override
    public Mono<Tenant> save(Tenant tenant) {
        return mongoRepository.save(mapper.toDocument(tenant))
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Tenant> findByTenantId(String tenantId) {
        return mongoRepository.findByTenantId(tenantId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Tenant> findBySlug(String slug) {
        return mongoRepository.findBySlug(slug)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return mongoRepository.count();
    }
}
