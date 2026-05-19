package com.spectrayan.synaptiq.integration.template;

import com.spectrayan.synaptiq.integration.model.TemplateDescriptor;
import com.spectrayan.synaptiq.integration.model.TemplateDescriptor.ParameterDefinition;

import java.util.List;

/**
 * Catalog of built-in integration template descriptors.
 * <p>
 * These are seed templates shipped with the library. They correspond to
 * the compiled Camel RouteTemplate definitions in {@link IntegrationRouteTemplates}.
 * Admins can create additional custom templates at runtime via the API —
 * those are stored in the database, not defined here.
 */
public final class BuiltInTemplates {

    private BuiltInTemplates() {
    }

    public static final TemplateDescriptor REST_API_POLL = TemplateDescriptor.builder()
            .templateId("rest-api-poll")
            .displayName("REST API Polling")
            .description("Periodically poll a REST API endpoint and process the response")
            .icon("http")
            .category("Data")
            .connectorType("REST_API")
            .builtIn(true)
            .requiresCredential(false)
            .parameters(List.of(
                    ParameterDefinition.builder()
                            .name("url").displayName("API URL").type("string")
                            .required(true).placeholder("https://api.example.com/data").build(),
                    ParameterDefinition.builder()
                            .name("method").displayName("HTTP Method").type("string")
                            .required(false).defaultValue("GET").build(),
                    ParameterDefinition.builder()
                            .name("authHeader").displayName("Authorization Header").type("secret")
                            .required(false).placeholder("Bearer ...").build(),
                    ParameterDefinition.builder()
                            .name("pollIntervalMs").displayName("Poll Interval (ms)").type("number")
                            .required(false).defaultValue("300000").build()
            ))
            .build();

    public static final TemplateDescriptor WEBHOOK_RECEIVER = TemplateDescriptor.builder()
            .templateId("webhook-receiver")
            .displayName("Webhook Receiver")
            .description("Receive inbound HTTP webhooks on a tenant-scoped endpoint")
            .icon("webhook")
            .category("Triggers")
            .connectorType("WEBHOOK")
            .builtIn(true)
            .requiresCredential(false)
            .parameters(List.of(
                    ParameterDefinition.builder()
                            .name("webhookPath").displayName("Webhook Path").type("string")
                            .required(true).placeholder("/my-webhook").build(),
                    ParameterDefinition.builder()
                            .name("targetEndpoint").displayName("Target Endpoint").type("string")
                            .required(false).placeholder("direct:process").build()
            ))
            .build();

    public static final TemplateDescriptor SLACK_NOTIFY = TemplateDescriptor.builder()
            .templateId("slack-notify")
            .displayName("Slack Notification")
            .description("Send messages to a Slack channel")
            .icon("slack")
            .category("Messaging")
            .connectorType("SLACK")
            .builtIn(true)
            .requiresCredential(true)
            .parameters(List.of(
                    ParameterDefinition.builder()
                            .name("channel").displayName("Channel").type("string")
                            .required(true).placeholder("#general").build(),
                    ParameterDefinition.builder()
                            .name("webhookUrl").displayName("Webhook URL").type("secret")
                            .required(true).placeholder("https://hooks.slack.com/...").build()
            ))
            .build();

    public static final TemplateDescriptor EMAIL_NOTIFY = TemplateDescriptor.builder()
            .templateId("email-notify")
            .displayName("Email Notification")
            .description("Send email notifications via SMTP")
            .icon("email")
            .category("Messaging")
            .connectorType("EMAIL")
            .builtIn(true)
            .requiresCredential(true)
            .parameters(List.of(
                    ParameterDefinition.builder()
                            .name("to").displayName("To Address").type("string")
                            .required(true).placeholder("alerts@example.com").build(),
                    ParameterDefinition.builder()
                            .name("subject").displayName("Subject").type("string")
                            .required(false).defaultValue("Synaptiq Notification").build(),
                    ParameterDefinition.builder()
                            .name("smtpHost").displayName("SMTP Host").type("string")
                            .required(true).placeholder("smtp.gmail.com").build(),
                    ParameterDefinition.builder()
                            .name("smtpPort").displayName("SMTP Port").type("number")
                            .required(false).defaultValue("587").build()
            ))
            .build();

    public static final TemplateDescriptor DATABASE_QUERY = TemplateDescriptor.builder()
            .templateId("db-query")
            .displayName("Database Query")
            .description("Periodically execute a SQL query against an external database")
            .icon("database")
            .category("Data")
            .connectorType("DATABASE")
            .builtIn(true)
            .requiresCredential(true)
            .parameters(List.of(
                    ParameterDefinition.builder()
                            .name("query").displayName("SQL Query").type("string")
                            .required(true).placeholder("SELECT * FROM orders WHERE created_at > NOW() - INTERVAL 1 DAY").build(),
                    ParameterDefinition.builder()
                            .name("pollIntervalMs").displayName("Poll Interval (ms)").type("number")
                            .required(false).defaultValue("600000").build()
            ))
            .build();

    public static final TemplateDescriptor KB_GOOGLE_DRIVE_INGEST = TemplateDescriptor.builder()
            .templateId("kb-google-drive-ingest")
            .displayName("Knowledge Base — Google Drive Ingestion")
            .description("Ingest documents from Google Drive into the Knowledge Base for RAG")
            .icon("google-drive")
            .category("KnowledgeBase")
            .connectorType("GOOGLE_DRIVE")
            .builtIn(true)
            .requiresCredential(true)
            .parameters(List.of(
                    ParameterDefinition.builder()
                            .name("folderId").displayName("Google Drive Folder ID").type("string")
                            .required(true).placeholder("1A2B3C4D5E...").build(),
                    ParameterDefinition.builder()
                            .name("categoryId").displayName("Knowledge Category ID").type("string")
                            .required(false).placeholder("Optional category for ingested docs").build(),
                    ParameterDefinition.builder()
                            .name("tags").displayName("Tags (comma-separated)").type("string")
                            .required(false).placeholder("hr,policy,onboarding").build(),
                    ParameterDefinition.builder()
                            .name("pollIntervalMs").displayName("Poll Interval (ms)").type("number")
                            .required(false).defaultValue("600000").build()
            ))
            .build();

    public static final TemplateDescriptor KB_ONEDRIVE_INGEST = TemplateDescriptor.builder()
            .templateId("kb-onedrive-ingest")
            .displayName("Knowledge Base — OneDrive Ingestion")
            .description("Ingest documents from OneDrive into the Knowledge Base for RAG")
            .icon("onedrive")
            .category("KnowledgeBase")
            .connectorType("ONEDRIVE")
            .builtIn(true)
            .requiresCredential(true)
            .parameters(List.of(
                    ParameterDefinition.builder()
                            .name("driveId").displayName("OneDrive Drive ID").type("string")
                            .required(true).placeholder("drive-id-xxx").build(),
                    ParameterDefinition.builder()
                            .name("folderPath").displayName("Folder Path").type("string")
                            .required(false).defaultValue("/").placeholder("/Documents/KB").build(),
                    ParameterDefinition.builder()
                            .name("categoryId").displayName("Knowledge Category ID").type("string")
                            .required(false).placeholder("Optional category for ingested docs").build(),
                    ParameterDefinition.builder()
                            .name("tags").displayName("Tags (comma-separated)").type("string")
                            .required(false).placeholder("engineering,specs").build(),
                    ParameterDefinition.builder()
                            .name("pollIntervalMs").displayName("Poll Interval (ms)").type("number")
                            .required(false).defaultValue("600000").build()
            ))
            .build();

    /**
     * All built-in templates.
     */
    public static List<TemplateDescriptor> all() {
        return List.of(
                REST_API_POLL,
                WEBHOOK_RECEIVER,
                SLACK_NOTIFY,
                EMAIL_NOTIFY,
                DATABASE_QUERY,
                KB_GOOGLE_DRIVE_INGEST,
                KB_ONEDRIVE_INGEST
        );
    }
}
