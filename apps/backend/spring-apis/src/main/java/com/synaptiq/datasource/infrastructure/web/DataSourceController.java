package com.synaptiq.datasource.infrastructure.web;

import com.synaptiq.datasource.application.port.in.DataSourceCrudUseCase;
import com.synaptiq.infrastructure.in.web.api.DataSourcesApi;
import com.synaptiq.infrastructure.in.web.dto.CreateDataSourceRequest;
import com.synaptiq.infrastructure.in.web.dto.DataSourceResponse;
import com.synaptiq.infrastructure.in.web.dto.ListDataSources200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * REST controller for the DataSource bounded context.
 * Implements the generated DataSourcesApi interface.
 */
@RestController
@RequiredArgsConstructor
public class DataSourceController implements DataSourcesApi {

    private final DataSourceCrudUseCase dataSourceCrud;
    private final DataSourceDtoMapper mapper;

    @Override
    public Mono<ResponseEntity<DataSourceResponse>> createDataSource(
            Mono<CreateDataSourceRequest> createDataSourceRequest,
            String xTenantID,
            ServerWebExchange exchange) {
        return createDataSourceRequest.flatMap(req -> {
            var command = new DataSourceCrudUseCase.CreateDataSourceCommand(
                xTenantID,
                req.getName(),
                req.getDescription(),
                req.getType() != null ? req.getType().getValue() : "SYNAPTIQ_NATIVE"
            );
            return dataSourceCrud.create(command)
                .map(mapper::toDto)
                .map(dto -> ResponseEntity.status(201).body(dto));
        });
    }

    @Override
    public Mono<ResponseEntity<DataSourceResponse>> getDataSource(
            String dataSourceId,
            String xTenantID,
            ServerWebExchange exchange) {
        return dataSourceCrud.getByDataSourceId(dataSourceId)
            .map(mapper::toDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<ListDataSources200Response>> listDataSources(
            String xTenantID,
            ServerWebExchange exchange) {
        return dataSourceCrud.listByTenantId(xTenantID)
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
        return dataSourceCrud.delete(dataSourceId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
