package com.spectrayan.synaptiq.action.application.port.out;

import com.spectrayan.synaptiq.action.domain.model.ActionLog;
import reactor.core.publisher.Mono;

/**
 * Outbound port for action log persistence.
 * Implemented by ActionLogMongoAdapter in the infrastructure layer.
 * Returns domain models — never MongoDB documents.
 */
public interface ActionLogPersistencePort {

    Mono<ActionLog> save(ActionLog actionLog);
}
