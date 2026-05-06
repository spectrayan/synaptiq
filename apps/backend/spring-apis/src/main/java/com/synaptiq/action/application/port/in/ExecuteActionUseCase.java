package com.synaptiq.action.application.port.in;

import reactor.core.publisher.Mono;

/**
 * Inbound port for executing a DSL form action.
 */
public interface ExecuteActionUseCase {

    Mono<ActionResult> execute(ExecuteActionCommand command);

    record ExecuteActionCommand(
        String tenantId,
        String appId,
        String action,
        String itemId,
        String sessionId,
        String userUid,
        String message
    ) {}

    record ActionResult(
        boolean success,
        String message,
        String resultId
    ) {}
}
