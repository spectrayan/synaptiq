package com.synaptiq.workflow.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synaptiq.agentflow.builder.models.settings.FlowSettings;
import com.synaptiq.workflow.domain.model.Workflow;
import com.synaptiq.infrastructure.in.web.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface WorkflowDtoMapper {

    @Mapping(target = "isPublic", source = "public")
    @Mapping(target = "spec", expression = "java(flowSettingsToDto(workflow.getSpec()))")
    WorkflowResponse toDto(Workflow workflow);

    default WorkflowListResponse toListDto(List<WorkflowResponse> workflows) {
        return new WorkflowListResponse().workflows(workflows);
    }

    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    /**
     * Maps the domain FlowSettings to the OpenAPI FlowSettingsSpec DTO.
     */
    @SuppressWarnings("unchecked")
    default FlowSettingsSpec flowSettingsToDto(FlowSettings spec) {
        if (spec == null) return null;
        ObjectMapper om = new ObjectMapper();
        var dto = new FlowSettingsSpec();
        dto.setId(spec.getId());
        dto.setName(spec.getName());
        dto.setEntrypoint(spec.getEntrypoint());
        if (spec.getFlowType() != null) {
            dto.setFlowType(FlowSettingsSpec.FlowTypeEnum.fromValue(spec.getFlowType().name()));
        }
        if (spec.getAgents() != null) {
            dto.setAgents(spec.getAgents().stream()
                .map(a -> om.convertValue(a, AgentSpec.class)).toList());
        }
        if (spec.getEdges() != null) {
            dto.setEdges(spec.getEdges().stream()
                .map(e -> om.convertValue(e, EdgeSpec.class)).toList());
        }
        if (spec.getMcpServers() != null) {
            dto.setMcpServers(spec.getMcpServers().stream()
                .map(m -> om.convertValue(m, MCPServerSpec.class)).toList());
        }
        if (spec.getPolicy() != null) {
            dto.setPolicy(om.convertValue(spec.getPolicy(), ExecutionPolicySpec.class));
        }
        return dto;
    }
}
