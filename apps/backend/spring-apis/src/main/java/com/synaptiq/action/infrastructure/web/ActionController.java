package com.synaptiq.action.infrastructure.web;

import com.synaptiq.action.application.port.in.ExecuteActionUseCase;
import com.synaptiq.action.application.port.in.SavedItemUseCase;
import com.synaptiq.infrastructure.in.web.api.ActionsApi;
import com.synaptiq.infrastructure.in.web.dto.ActionResultResponse;
import com.synaptiq.infrastructure.in.web.dto.ExecuteActionRequest;
import com.synaptiq.infrastructure.in.web.dto.SavedItemListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * REST controller for the Action module.
 * Implements the generated ActionsApi interface — all annotations come from the spec.
 */
@RestController
@RequiredArgsConstructor
public class ActionController implements ActionsApi {

    private final ExecuteActionUseCase executeActionUseCase;
    private final SavedItemUseCase savedItemUseCase;
    private final ActionDtoMapper mapper;

    @Override
    public Mono<ResponseEntity<ActionResultResponse>> executeAction(
            Mono<ExecuteActionRequest> executeActionRequest,
            String xTenantID,
            ServerWebExchange exchange) {
        return executeActionRequest.flatMap(req -> {
            var command = new ExecuteActionUseCase.ExecuteActionCommand(
                xTenantID,
                null, // appId — resolved from X-App-ID header
                req.getAction(),
                req.getItemId(),
                req.getSessionId(),
                null, // userUid — resolved from auth context
                req.getMessage()
            );
            return executeActionUseCase.execute(command)
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
        });
    }

    @Override
    public Mono<ResponseEntity<SavedItemListResponse>> listSavedItems(
            String xTenantID,
            String sessionId,
            ServerWebExchange exchange) {
        return savedItemUseCase.getSavedItems(xTenantID, sessionId)
            .map(mapper::toDto)
            .collectList()
            .map(mapper::toListDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> removeSavedItem(
            String itemId,
            String xTenantID,
            String sessionId,
            ServerWebExchange exchange) {
        return savedItemUseCase.removeSavedItem(xTenantID, itemId, sessionId)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
