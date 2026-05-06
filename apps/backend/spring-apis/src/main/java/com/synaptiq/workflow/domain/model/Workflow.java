package com.synaptiq.workflow.domain.model;

import com.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Aggregate root for workflows.
 * <p>
 * {@code spec} is now typed as {@link FlowSettings} from the agent-flow-spring
 * library instead of a raw {@code Map<String, Object>}.
 */
@Getter @Setter @SuperBuilder @NoArgsConstructor
public class Workflow extends AggregateRoot {
    private String tenantId;
    private String appId;
    private FlowSettings spec;
    private String shareToken;
    @Builder.Default private boolean isPublic = false;
}
