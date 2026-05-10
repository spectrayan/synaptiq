package com.spectrayan.synaptiq.datasource.application.port.in;

import com.spectrayan.synaptiq.datasource.domain.model.DataSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Inbound port for DataSource read operations (get, list).
 */
public interface DataSourceQueryUseCase {

    Mono<DataSource> getByDataSourceId(String dataSourceId);

    Flux<DataSource> listByTenantId(String tenantId);
}
