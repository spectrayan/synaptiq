package com.spectrayan.synaptiq.integration.template;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Defines parameterized Camel RouteTemplates for built-in integration patterns.
 * <p>
 * Each template corresponds to a {@link BuiltInTemplates} descriptor.
 * Tenants instantiate these with their specific parameters via
 * {@link TemplateRegistry#instantiate}.
 */
@Slf4j
@Component
public class IntegrationRouteTemplates extends RouteBuilder {

    @Override
    public void configure() {

        // ── REST API Polling ──────────────────────────────────────
        routeTemplate("rest-api-poll")
                .templateParameter("tenantId")
                .templateParameter("routeId")
                .templateParameter("url")
                .templateParameter("method", "GET")
                .templateParameter("authHeader", "")
                .templateParameter("pollIntervalMs", "300000")
                .from("timer:{{routeId}}?period={{pollIntervalMs}}")
                .routeId("{{routeId}}")
                .setHeader("CamelHttpMethod", simple("{{method}}"))
                .setHeader("Authorization", simple("{{authHeader}}"))
                .to("{{url}}")
                .log("REST poll completed for tenant {{tenantId}}: ${header.CamelHttpResponseCode}");

        // ── Webhook Receiver ──────────────────────────────────────
        routeTemplate("webhook-receiver")
                .templateParameter("tenantId")
                .templateParameter("routeId")
                .templateParameter("webhookPath")
                .templateParameter("targetEndpoint", "log:webhook-received")
                .from("platform-http:/webhooks/{{tenantId}}{{webhookPath}}")
                .routeId("{{routeId}}")
                .log("Webhook received for tenant {{tenantId}}: ${body}")
                .to("{{targetEndpoint}}");

        // ── Slack Notification ────────────────────────────────────
        routeTemplate("slack-notify")
                .templateParameter("tenantId")
                .templateParameter("routeId")
                .templateParameter("channel")
                .templateParameter("webhookUrl", "")
                .from("direct:{{routeId}}")
                .routeId("{{routeId}}")
                .setHeader("CamelSlackChannel", constant("{{channel}}"))
                .to("slack:#{{channel}}");

        // ── Email Notification ────────────────────────────────────
        routeTemplate("email-notify")
                .templateParameter("tenantId")
                .templateParameter("routeId")
                .templateParameter("to")
                .templateParameter("subject", "Synaptiq Notification")
                .templateParameter("smtpHost")
                .templateParameter("smtpPort", "587")
                .from("direct:{{routeId}}")
                .routeId("{{routeId}}")
                .setHeader("To", constant("{{to}}"))
                .setHeader("Subject", constant("{{subject}}"))
                .to("smtp://{{smtpHost}}:{{smtpPort}}");

        // ── Database Query ────────────────────────────────────────
        routeTemplate("db-query")
                .templateParameter("tenantId")
                .templateParameter("routeId")
                .templateParameter("query")
                .templateParameter("pollIntervalMs", "600000")
                .from("timer:{{routeId}}?period={{pollIntervalMs}}")
                .routeId("{{routeId}}")
                .setBody(constant("{{query}}"))
                .to("jdbc:dataSource")
                .log("DB query for tenant {{tenantId}} returned ${body.size()} rows");

        // ── Knowledge Base — Google Drive Ingestion ─────────────────
        routeTemplate("kb-google-drive-ingest")
                .templateParameter("tenantId")
                .templateParameter("routeId")
                .templateParameter("folderId")
                .templateParameter("categoryId", "")
                .templateParameter("tags", "")
                .templateParameter("pollIntervalMs", "600000")
                .from("timer:{{routeId}}?period={{pollIntervalMs}}")
                .routeId("{{routeId}}")
                .setHeader("tenantId", simple("{{tenantId}}"))
                .setHeader("categoryId", simple("{{categoryId}}"))
                .setHeader("tags", simple("{{tags}}"))
                .setHeader("sourceType", constant("GOOGLE_DRIVE"))
                .setHeader("folderId", simple("{{folderId}}"))
                .to("direct:kb-ingest")
                .log("KB Google Drive poll completed for tenant {{tenantId}}, folder {{folderId}}");

        // ── Knowledge Base — OneDrive Ingestion ──────────────────────
        routeTemplate("kb-onedrive-ingest")
                .templateParameter("tenantId")
                .templateParameter("routeId")
                .templateParameter("driveId")
                .templateParameter("folderPath", "/")
                .templateParameter("categoryId", "")
                .templateParameter("tags", "")
                .templateParameter("pollIntervalMs", "600000")
                .from("timer:{{routeId}}?period={{pollIntervalMs}}")
                .routeId("{{routeId}}")
                .setHeader("tenantId", simple("{{tenantId}}"))
                .setHeader("categoryId", simple("{{categoryId}}"))
                .setHeader("tags", simple("{{tags}}"))
                .setHeader("sourceType", constant("ONEDRIVE"))
                .setHeader("driveId", simple("{{driveId}}"))
                .setHeader("folderPath", simple("{{folderPath}}"))
                .to("direct:kb-ingest")
                .log("KB OneDrive poll completed for tenant {{tenantId}}, drive {{driveId}}");

        log.info("Registered {} integration route templates",
                7);
    }
}
