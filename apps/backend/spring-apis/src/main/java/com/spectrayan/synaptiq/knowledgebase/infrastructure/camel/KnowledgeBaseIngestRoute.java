package com.spectrayan.synaptiq.knowledgebase.infrastructure.camel;

import com.spectrayan.synaptiq.knowledgebase.application.port.in.DocumentUploadUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Camel route consumer for Knowledge Base ingestion.
 * <p>
 * This route listens on {@code direct:kb-ingest} — the common endpoint
 * that all dynamically-instantiated KB route templates (Google Drive, OneDrive, etc.)
 * route to. This decouples the ingestion logic from the transport/polling mechanism.
 * <p>
 * Route templates are defined in the {@code camel-integration} library's
 * {@code IntegrationRouteTemplates} and instantiated dynamically via
 * {@code TemplateRegistry.instantiate()} — no hardcoded source-specific routes here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeBaseIngestRoute extends RouteBuilder {

    private final DocumentUploadUseCase documentUploadUseCase;

    @Override
    public void configure() {

        // ── Error Handler ───────────────────────────────────────────────────
        onException(Exception.class)
                .handled(true)
                .log("ERROR: KB ingestion failed for ${header.fileName}: ${exception.message}")
                .maximumRedeliveries(2)
                .redeliveryDelay(3000);

        // ── Common KB Ingestion Endpoint ─────────────────────────────────────
        // All dynamic KB route templates (Google Drive, OneDrive, etc.) route here.
        // Expects:
        //   body:   byte[] (file content)
        //   headers: tenantId, fileName, categoryId (optional), tags (optional, comma-separated), sourceType
        from("direct:kb-ingest")
                .routeId("kb-ingest-processor")
                .log("KB Ingestion: ${header.fileName} (tenant: ${header.tenantId}, source: ${header.sourceType})")
                .process(exchange -> {
                    byte[] fileBytes = exchange.getIn().getBody(byte[].class);
                    String tenantId = exchange.getIn().getHeader("tenantId", String.class);
                    String fileName = exchange.getIn().getHeader("fileName", String.class);
                    String categoryId = exchange.getIn().getHeader("categoryId", String.class);
                    String tagsHeader = exchange.getIn().getHeader("tags", String.class);

                    List<String> tags = (tagsHeader != null && !tagsHeader.isBlank())
                            ? Arrays.asList(tagsHeader.split(","))
                            : List.of();

                    ByteArrayResource resource = new ByteArrayResource(fileBytes);
                    String docId = documentUploadUseCase
                            .ingestDocument(tenantId, categoryId, tags, resource, fileName)
                            .block();

                    exchange.getIn().setHeader("documentId", docId);
                })
                .log("KB Ingestion complete: ${header.fileName} → docId=${header.documentId}");
    }
}
