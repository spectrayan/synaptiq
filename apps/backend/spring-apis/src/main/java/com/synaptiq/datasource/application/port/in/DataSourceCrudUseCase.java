package com.synaptiq.datasource.application.port.in;

import com.synaptiq.datasource.domain.model.DataSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Inbound port for DataSource CRUD operations.
 */
public interface DataSourceCrudUseCase {

    Mono<DataSource> create(CreateDataSourceCommand command);

    Mono<DataSource> getByDataSourceId(String dataSourceId);

    Flux<DataSource> listByTenantId(String tenantId);

    Mono<Void> delete(String dataSourceId);

    record CreateDataSourceCommand(
        String tenantId,
        String name,
        String description,
        String type
    ) {}
}
