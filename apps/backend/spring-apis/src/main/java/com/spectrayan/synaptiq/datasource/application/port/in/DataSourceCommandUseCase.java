package com.spectrayan.synaptiq.datasource.application.port.in;

import com.spectrayan.synaptiq.datasource.domain.model.DataSource;
import reactor.core.publisher.Mono;

/**
 * Inbound port for DataSource write operations (create, delete).
 */
public interface DataSourceCommandUseCase {

    Mono<DataSource> create(CreateDataSourceCommand command);

    Mono<Void> delete(String dataSourceId);

    record CreateDataSourceCommand(
        String tenantId,
        String name,
        String description,
        String type
    ) {}
}
