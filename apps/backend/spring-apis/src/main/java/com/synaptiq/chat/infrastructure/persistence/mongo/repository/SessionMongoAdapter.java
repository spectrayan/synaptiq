package com.synaptiq.chat.infrastructure.persistence.mongo.repository;

import com.synaptiq.chat.application.port.out.SessionPersistencePort;
import com.synaptiq.chat.domain.model.Session;
import com.synaptiq.chat.infrastructure.persistence.mongo.entity.SessionDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;

/**
 * Session persistence adapter — direct typed mapping, no Jackson Map conversion.
 */
@Component @RequiredArgsConstructor
public class SessionMongoAdapter implements SessionPersistencePort {
    private final ReactiveMongoTemplate mongoTemplate;

    @Override public Mono<Session> save(Session session) {
        return mongoTemplate.save(toDoc(session)).map(this::toDomain);
    }

    @Override public Mono<Session> findBySessionIdAndTenantId(String sessionId, String tenantId) {
        return mongoTemplate.findOne(Query.query(Criteria.where("sessionId").is(sessionId).and("tenantId").is(tenantId)), SessionDocument.class)
            .map(this::toDomain);
    }

    @Override public Flux<Session> findByTenantId(String tenantId, int limit, int offset) {
        var q = Query.query(Criteria.where("tenantId").is(tenantId))
            .with(PageRequest.of(offset / Math.max(limit, 1), limit, Sort.by(Sort.Direction.DESC, "updatedAt")));
        return mongoTemplate.find(q, SessionDocument.class).map(this::toDomain);
    }

    @Override public Mono<Long> countByTenantId(String tenantId) {
        return mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId)), SessionDocument.class);
    }

    @Override public Mono<Void> deleteBySessionIdAndTenantId(String sessionId, String tenantId) {
        return mongoTemplate.remove(Query.query(Criteria.where("sessionId").is(sessionId).and("tenantId").is(tenantId)), SessionDocument.class).then();
    }

    private SessionDocument toDoc(Session s) {
        return SessionDocument.builder()
            .id(s.getId()).sessionId(s.getSessionId()).tenantId(s.getTenantId()).appId(s.getAppId())
            .userUid(s.getUserUid()).title(s.getTitle())
            .metadata(mapMetadata(s.getMetadata()))
            .pinnedViews(s.getPinnedViews() != null ? s.getPinnedViews().stream().map(this::mapPinnedView).toList() : List.of())
            .turns(s.getTurns() != null ? s.getTurns().stream().map(this::mapTurn).toList() : List.of())
            .activeFilters(s.getActiveFilters() != null ? s.getActiveFilters().stream().map(this::mapFilter).toList() : List.of())
            .expiresAt(s.getExpiresAt()).createdAt(s.getCreatedAt()).updatedAt(s.getUpdatedAt())
            .build();
    }

    private Session toDomain(SessionDocument d) {
        return Session.builder()
            .id(d.getId()).sessionId(d.getSessionId()).tenantId(d.getTenantId()).appId(d.getAppId())
            .userUid(d.getUserUid()).title(d.getTitle())
            .metadata(mapMetadataToDomain(d.getMetadata()))
            .pinnedViews(d.getPinnedViews() != null ? d.getPinnedViews().stream().map(this::mapPinnedViewToDomain).toList() : new ArrayList<>())
            .turns(d.getTurns() != null ? d.getTurns().stream().map(this::mapTurnToDomain).toList() : new ArrayList<>())
            .activeFilters(d.getActiveFilters() != null ? d.getActiveFilters().stream().map(this::mapFilterToDomain).toList() : new ArrayList<>())
            .expiresAt(d.getExpiresAt()).createdAt(d.getCreatedAt()).updatedAt(d.getUpdatedAt())
            .build();
    }

    // ── Domain → Document mappers ───────────────────────────────────

    private SessionDocument.ConversationTurnEmbed mapTurn(Session.ConversationTurn t) {
        return SessionDocument.ConversationTurnEmbed.builder()
            .turnId(t.getTurnId()).role(t.getRole()).content(t.getContent())
            .uiComponents(t.getUiComponents() != null ? t.getUiComponents().stream().map(this::mapUiComponent).toList() : List.of())
            .tokenCountInput(t.getTokenCountInput()).tokenCountOutput(t.getTokenCountOutput())
            .modelId(t.getModelId()).createdAt(t.getCreatedAt())
            .build();
    }

    private SessionDocument.UIComponentEmbed mapUiComponent(Session.UIComponent c) {
        return SessionDocument.UIComponentEmbed.builder()
            .componentType(c.getComponentType()).componentId(c.getComponentId())
            .label(c.getLabel()).payload(c.getPayload())
            .build();
    }

    private SessionDocument.ActiveFilterEmbed mapFilter(Session.ActiveFilter f) {
        return SessionDocument.ActiveFilterEmbed.builder()
            .fieldId(f.getFieldId()).operator(f.getOperator()).value(f.getValue())
            .build();
    }

    private SessionDocument.SessionMetadataEmbed mapMetadata(Session.SessionMetadata m) {
        if (m == null) return null;
        return SessionDocument.SessionMetadataEmbed.builder()
            .deviceType(m.getDeviceType()).locale(m.getLocale()).referrer(m.getReferrer())
            .userAgent(m.getUserAgent()).appVersion(m.getAppVersion())
            .build();
    }

    private SessionDocument.PinnedViewEmbed mapPinnedView(Session.PinnedView v) {
        return SessionDocument.PinnedViewEmbed.builder()
            .viewId(v.getViewId()).viewType(v.getViewType()).title(v.getTitle())
            .dataSourceId(v.getDataSourceId()).pinnedAt(v.getPinnedAt())
            .build();
    }

    // ── Document → Domain mappers ───────────────────────────────────

    private Session.ConversationTurn mapTurnToDomain(SessionDocument.ConversationTurnEmbed t) {
        return Session.ConversationTurn.builder()
            .turnId(t.getTurnId()).role(t.getRole()).content(t.getContent())
            .uiComponents(t.getUiComponents() != null ? t.getUiComponents().stream().map(this::mapUiComponentToDomain).toList() : new ArrayList<>())
            .tokenCountInput(t.getTokenCountInput()).tokenCountOutput(t.getTokenCountOutput())
            .modelId(t.getModelId()).createdAt(t.getCreatedAt())
            .build();
    }

    private Session.UIComponent mapUiComponentToDomain(SessionDocument.UIComponentEmbed c) {
        return Session.UIComponent.builder()
            .componentType(c.getComponentType()).componentId(c.getComponentId())
            .label(c.getLabel()).payload(c.getPayload())
            .build();
    }

    private Session.ActiveFilter mapFilterToDomain(SessionDocument.ActiveFilterEmbed f) {
        return Session.ActiveFilter.builder()
            .fieldId(f.getFieldId()).operator(f.getOperator()).value(f.getValue())
            .build();
    }

    private Session.SessionMetadata mapMetadataToDomain(SessionDocument.SessionMetadataEmbed m) {
        if (m == null) return null;
        return Session.SessionMetadata.builder()
            .deviceType(m.getDeviceType()).locale(m.getLocale()).referrer(m.getReferrer())
            .userAgent(m.getUserAgent()).appVersion(m.getAppVersion())
            .build();
    }

    private Session.PinnedView mapPinnedViewToDomain(SessionDocument.PinnedViewEmbed v) {
        return Session.PinnedView.builder()
            .viewId(v.getViewId()).viewType(v.getViewType()).title(v.getTitle())
            .dataSourceId(v.getDataSourceId()).pinnedAt(v.getPinnedAt())
            .build();
    }
}
