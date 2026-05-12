package com.spectrayan.synaptiq.datasource.application.port.out;

import com.spectrayan.synaptiq.datasource.domain.model.DataSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Outbound port for DataSource persistence.
 * Returns domain models — never MongoDB documents.
 */
public interface DataSourcePersistencePort {

    Mono<DataSource> save(DataSource dataSource);

    Mono<DataSource> findByDataSourceId(String dataSourceId);

    Flux<DataSource> findAllByTenantId(String tenantId);

    Mono<Long> countByTenantId(String tenantId);

    Mono<Void> deleteByDataSourceId(String dataSourceId);
}
