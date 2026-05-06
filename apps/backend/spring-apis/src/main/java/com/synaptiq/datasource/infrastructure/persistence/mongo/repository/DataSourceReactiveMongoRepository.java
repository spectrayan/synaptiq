package com.synaptiq.datasource.infrastructure.persistence.mongo.repository;

import com.synaptiq.datasource.infrastructure.persistence.mongo.entity.DataSourceDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DataSourceReactiveMongoRepository extends ReactiveMongoRepository<DataSourceDocument, String> {
    Mono<DataSourceDocument> findByDataSourceId(String dataSourceId);
    Flux<DataSourceDocument> findByTenantId(String tenantId);
    Mono<Long> countByTenantId(String tenantId);
    Mono<Void> deleteByDataSourceId(String dataSourceId);
}
