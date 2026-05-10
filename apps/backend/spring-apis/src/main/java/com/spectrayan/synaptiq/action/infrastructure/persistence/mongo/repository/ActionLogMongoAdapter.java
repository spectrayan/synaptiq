package com.spectrayan.synaptiq.action.infrastructure.persistence.mongo.repository;

import com.spectrayan.synaptiq.action.application.port.out.ActionLogPersistencePort;
import com.spectrayan.synaptiq.action.domain.model.ActionLog;
import com.spectrayan.synaptiq.action.infrastructure.persistence.mongo.mapper.ActionPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Adapter implementing the domain's {@link ActionLogPersistencePort}.
 * Converts between domain models and MongoDB documents using MapStruct.
 */
@Component
@RequiredArgsConstructor
public class ActionLogMongoAdapter implements ActionLogPersistencePort {

    private final ActionLogReactiveMongoRepository mongoRepository;
    private final ActionPersistenceMapper mapper;

    @Override
    public Mono<ActionLog> save(ActionLog actionLog) {
        return mongoRepository.save(mapper.toDocument(actionLog))
            .map(mapper::toDomain);
    }
}
