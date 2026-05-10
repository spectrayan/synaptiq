package com.spectrayan.synaptiq.notification.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enumeration of all known notification event types.
 * <p>
 * Provides metadata (icon, display title, message template) used when creating
 * notification records. Message templates use {@code {key}} placeholders that
 * are resolved against the event payload map.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationEventType {

    WORKFLOW_GENERATED("workflow.generated",     "auto_awesome",  "Workflow Generated",
            "Workflow \"{workflowName}\" generated from prompt"),

    WORKFLOW_COMPLETED("workflow.completed",     "check_circle",  "Workflow Completed",
            "Workflow \"{workflowName}\" completed successfully"),

    WORKFLOW_FAILED("workflow.failed",           "error",         "Workflow Failed",
            "Workflow \"{workflowName}\" failed: {error}"),

    WORKFLOW_SHARED("workflow.shared",           "share",         "Workflow Shared",
            "Workflow \"{workflowName}\" is now shared"),

    DATA_IMPORTED("data.imported",               "upload_file",   "Data Import Complete",
            "Imported {count} records into data source"),

    TENANT_CREATED("tenant.created",             "domain_add",    "Tenant Created",
            "Tenant \"{tenantName}\" has been provisioned"),

    CHAT_SESSION_CREATED("chat.session_created", "chat",          "Chat Session",
            "New chat session started"),

    LLM_ERROR("llm.error",                       "warning",       "LLM Service Error",
            "LLM call failed: {error}");

    private final String key;
    private final String icon;
    private final String title;
    private final String messageTemplate;

    /**
     * Resolves the message template against a payload map.
     * Replaces all {@code {key}} placeholders with values from the map.
     * Missing keys are replaced with empty strings.
     */
    public String resolveMessage(Map<String, Object> payload) {
        String result = messageTemplate;
        if (payload != null) {
            for (var entry : payload.entrySet()) {
                String placeholder = "{" + entry.getKey() + "}";
                String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
                result = result.replace(placeholder, value);
            }
        }
        result = result.replaceAll("\\{[^}]+}", "").trim();
        result = result.replaceAll(":\\s*$", "").trim();
        return result;
    }

    /**
     * Returns all event type keys — used as the default "all enabled" set.
     */
    public static Set<String> allKeys() {
        return Arrays.stream(values())
                .map(NotificationEventType::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Lookup by key string, returns null if not found.
     */
    public static NotificationEventType fromKey(String key) {
        for (NotificationEventType t : values()) {
            if (t.key.equals(key)) return t;
        }
        return null;
    }
}
