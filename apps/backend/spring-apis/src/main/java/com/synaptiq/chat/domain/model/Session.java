package com.synaptiq.chat.domain.model;

import com.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Session extends AggregateRoot {
    private String sessionId;
    private String tenantId;
    private String appId;
    private String userUid;
    private String title;
    @Builder.Default private List<ConversationTurn> turns = new ArrayList<>();
    @Builder.Default private List<ActiveFilter> activeFilters = new ArrayList<>();
    private SessionMetadata metadata;
    @Builder.Default private List<PinnedView> pinnedViews = new ArrayList<>();
    private Instant expiresAt;

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class ConversationTurn {
        private String turnId;
        private String role;
        @Builder.Default private String content = "";
        @Builder.Default private List<UIComponent> uiComponents = new ArrayList<>();
        @Builder.Default private int tokenCountInput = 0;
        @Builder.Default private int tokenCountOutput = 0;
        @Builder.Default private String modelId = "";
        @Builder.Default private Instant createdAt = Instant.now();
    }

    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class ActiveFilter {
        private String fieldId;
        @Builder.Default private String operator = "eq";
        private String value;
    }

    /**
     * Typed session metadata — replaces raw Map.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class SessionMetadata {
        private String deviceType;
        private String locale;
        private String referrer;
        private String userAgent;
        private String appVersion;
    }

    /**
     * Typed pinned view — replaces raw Map.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class PinnedView {
        private String viewId;
        private String viewType;
        private String title;
        private String dataSourceId;
        @Builder.Default private Instant pinnedAt = Instant.now();
    }

    /**
     * Typed UI component in a conversation turn — replaces raw Map.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class UIComponent {
        private String componentType;
        private String componentId;
        private String label;
        private String payload;
    }
}
