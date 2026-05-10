package com.spectrayan.synaptiq.application.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.application.application.port.out.ApplicationPersistencePort;
import com.spectrayan.synaptiq.application.domain.model.Application;
import com.spectrayan.synaptiq.application.infrastructure.persistence.mongo.mapper.ApplicationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adapter implementing the domain's {@link ApplicationPersistencePort}.
 * Converts between domain models and MongoDB documents using MapStruct.
 */
@Component
@RequiredArgsConstructor
public class ApplicationMongoAdapter implements ApplicationPersistencePort {

    private final ApplicationReactiveMongoRepository mongoRepository;
    private final ApplicationPersistenceMapper mapper;

    @Override
    public Mono<Application> save(Application application) {
        return mongoRepository.save(mapper.toDocument(application))
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Application> findByAppId(String appId) {
        return mongoRepository.findByAppId(appId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Application> findDefaultByTenantId(String tenantId) {
        return mongoRepository.findByTenantIdAndIsDefaultTrue(tenantId)
            .map(mapper::toDomain);
    }

    @Override
    public Flux<Application> findAllByTenantId(String tenantId) {
        return mongoRepository.findByTenantId(tenantId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countByTenantId(String tenantId) {
        return mongoRepository.countByTenantId(tenantId);
    }

    @Override
    public Mono<Void> deleteByAppId(String appId) {
        return mongoRepository.deleteByAppId(appId);
    }
}
