package com.synaptiq.chat.infrastructure.web;

import com.synaptiq.chat.domain.model.Session;
import com.synaptiq.infrastructure.in.web.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatDtoMapper {

    @Mapping(target = "turnCount", expression = "java(session.getTurns() != null ? session.getTurns().size() : 0)")
    SessionResponse toSessionDto(Session session);

    default SessionSummaryResponse toSummaryDto(Session session) {
        return new SessionSummaryResponse()
            .sessionId(session.getSessionId())
            .title(session.getTitle())
            .turnCount(session.getTurns() != null ? session.getTurns().size() : 0);
    }

    default SessionListResponse toListDto(List<SessionSummaryResponse> sessions, long total) {
        return new SessionListResponse().sessions(sessions).total(total);
    }

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }
}
