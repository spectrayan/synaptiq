package com.spectrayan.synaptiq.auth.infrastructure.persistence.mongo;

import com.spectrayan.synaptiq.auth.application.port.out.RolePersistencePort;
import com.spectrayan.synaptiq.auth.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;

@Component
@RequiredArgsConstructor
public class RoleMongoAdapter implements RolePersistencePort {

    private final RoleMongoRepository repository;

    @Override
    public Mono<Role> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public Flux<Role> findByTenantIdOrGlobal(String tenantId) {
        return repository.findByTenantIdOrTenantIdIsNull(tenantId).map(this::toDomain);
    }

    @Override
    public Flux<Role> findAll() {
        return repository.findAll().map(this::toDomain);
    }

    @Override
    public Mono<Role> save(Role role) {
        return repository.save(toDocument(role)).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteBySlug(String slug) {
        return repository.deleteBySlug(slug);
    }

    @Override
    public Mono<Boolean> existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    private Role toDomain(RoleDocument doc) {
        return Role.builder()
            .id(doc.getId())
            .slug(doc.getSlug())
            .tenantId(doc.getTenantId())
            .displayName(doc.getDisplayName())
            .description(doc.getDescription())
            .systemRole(doc.isSystemRole())
            .roleType(doc.getRoleType())
            .scopeSlugs(doc.getScopeSlugs() != null ? new HashSet<>(doc.getScopeSlugs()) : new HashSet<>())
            .build();
    }

    private RoleDocument toDocument(Role role) {
        var doc = new RoleDocument();
        doc.setId(role.getId());
        doc.setSlug(role.getSlug());
        doc.setTenantId(role.getTenantId());
        doc.setDisplayName(role.getDisplayName());
        doc.setDescription(role.getDescription());
        doc.setSystemRole(role.isSystemRole());
        doc.setRoleType(role.getRoleType());
        doc.setScopeSlugs(role.getScopeSlugs());
        return doc;
    }
}
