package com.spectrayan.synaptiq.datasource.infrastructure.web;

import com.spectrayan.synaptiq.datasource.application.port.in.DataSourceCommandUseCase;
import com.spectrayan.synaptiq.datasource.application.port.in.DataSourceQueryUseCase;
import com.spectrayan.synaptiq.infrastructure.in.web.api.DataSourcesApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.CreateDataSourceRequest;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.DataSourceResponse;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.ListDataSources200Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * REST controller for the DataSource bounded context.
 * Implements the generated DataSourcesApi interface.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DataSourceController implements DataSourcesApi {

    private final DataSourceCommandUseCase dataSourceCommand;
    private final DataSourceQueryUseCase dataSourceQuery;
    private final DataSourceDtoMapper mapper;

    @Override
    public Mono<ResponseEntity<DataSourceResponse>> createDataSource(
            Mono<CreateDataSourceRequest> createDataSourceRequest,
            String xTenantID,
            ServerWebExchange exchange) {
        return createDataSourceRequest.flatMap(req -> {
            var command = new DataSourceCommandUseCase.CreateDataSourceCommand(
                xTenantID,
                req.getName(),
                req.getDescription(),
                req.getType() != null ? req.getType().getValue() : "SYNAPTIQ_NATIVE"
            );
            return dataSourceCommand.create(command)
                .map(mapper::toDto)
                .map(dto -> ResponseEntity.status(201).body(dto));
        });
    }

    @Override
    public Mono<ResponseEntity<DataSourceResponse>> getDataSource(
            String dataSourceId,
            String xTenantID,
            ServerWebExchange exchange) {
        return dataSourceQuery.getByDataSourceId(dataSourceId)
            .map(mapper::toDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<ListDataSources200Response>> listDataSources(
            String xTenantID,
            ServerWebExchange exchange) {
        return dataSourceQuery.listByTenantId(xTenantID)
            .map(mapper::toDto)
            .collectList()
            .map(dsList -> ResponseEntity.ok(
                new ListDataSources200Response().dataSources(dsList)));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteDataSource(
            String dataSourceId,
            String xTenantID,
            ServerWebExchange exchange) {
        return dataSourceCommand.delete(dataSourceId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
