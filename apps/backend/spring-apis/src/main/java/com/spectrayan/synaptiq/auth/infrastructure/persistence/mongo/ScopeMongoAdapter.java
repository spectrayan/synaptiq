package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.auth.application.port.out.ScopePersistencePort;
import com.spectrayan.synaptiq.auth.domain.model.Scope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ScopeMongoAdapter implements ScopePersistencePort {

    private final ScopeMongoRepository repository;

    @Override
    public Flux<Scope> findAll() {
        return repository.findAll().map(this::toDomain);
    }

    @Override
    public Flux<Scope> findByResource(String resource) {
        return repository.findByResource(resource).map(this::toDomain);
    }

    @Override
    public Mono<Scope> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public Mono<Scope> save(Scope scope) {
        return repository.save(toDocument(scope)).map(this::toDomain);
    }

    @Override
    public Mono<Long> count() {
        return repository.count();
    }

    private Scope toDomain(ScopeDocument doc) {
        return Scope.builder()
            .slug(doc.getSlug())
            .displayName(doc.getDisplayName())
            .description(doc.getDescription())
            .resource(doc.getResource())
            .action(doc.getAction())
            .build();
    }

    private ScopeDocument toDocument(Scope scope) {
        var doc = new ScopeDocument();
        doc.setSlug(scope.getSlug());
        doc.setDisplayName(scope.getDisplayName());
        doc.setDescription(scope.getDescription());
        doc.setResource(scope.getResource());
        doc.setAction(scope.getAction());
        return doc;
    }
}
