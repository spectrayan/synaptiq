package com.synaptiq.chat.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Data @Builder
@Document(collection = "sessions")
@CompoundIndex(name = "idx_session_tenant", def = "{'sessionId': 1, 'tenantId': 1}", unique = true)
public class SessionDocument {
    @Id private String id;
    private String sessionId;
    private String tenantId;
    private String appId;
    private String userUid;
    private String title;
    private List<ConversationTurnEmbed> turns;
    private List<ActiveFilterEmbed> activeFilters;
    private SessionMetadataEmbed metadata;
    private List<PinnedViewEmbed> pinnedViews;
    private Instant expiresAt;
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ConversationTurnEmbed {
        private String turnId;
        private String role;
        private String content;
        private List<UIComponentEmbed> uiComponents;
        private int tokenCountInput;
        private int tokenCountOutput;
        private String modelId;
        private Instant createdAt;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class UIComponentEmbed {
        private String componentType;
        private String componentId;
        private String label;
        private String payload;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ActiveFilterEmbed {
        private String fieldId;
        private String operator;
        private String value;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class SessionMetadataEmbed {
        private String deviceType;
        private String locale;
        private String referrer;
        private String userAgent;
        private String appVersion;
    }

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class PinnedViewEmbed {
        private String viewId;
        private String viewType;
        private String title;
        private String dataSourceId;
        private Instant pinnedAt;
    }
}
