package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.auth.application.port.out.PathScopeMappingPort;
import com.spectrayan.synaptiq.auth.domain.model.PathScopeMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PathScopeMappingMongoAdapter implements PathScopeMappingPort {

    private final PathScopeMappingMongoRepository repository;

    @Override
    public Flux<PathScopeMapping> findAll() {
        return repository.findAll().map(this::toDomain);
    }

    @Override
    public Mono<PathScopeMapping> save(PathScopeMapping mapping) {
        return repository.save(toDocument(mapping)).map(this::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return repository.count();
    }

    private PathScopeMapping toDomain(PathScopeMappingDocument doc) {
        return PathScopeMapping.builder()
            .id(doc.getId())
            .httpMethod(doc.getHttpMethod())
            .pathPattern(doc.getPathPattern())
            .requiredScope(doc.getRequiredScope())
            .build();
    }

    private PathScopeMappingDocument toDocument(PathScopeMapping m) {
        var doc = new PathScopeMappingDocument();
        doc.setId(m.getId());
        doc.setHttpMethod(m.getHttpMethod());
        doc.setPathPattern(m.getPathPattern());
        doc.setRequiredScope(m.getRequiredScope());
        return doc;
    }
}
