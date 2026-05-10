package com.synaptiq.workflow.application.service;

import com.synaptiq.workflow.application.port.in.WorkflowToolRegistryUseCase;
import com.synaptiq.workflow.domain.model.ToolDefinition;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * In-memory tool registry with built-in tool definitions.
 * Tools are the actions agents can invoke during workflow execution.
 */
@Service
public class ToolRegistryService implements WorkflowToolRegistryUseCase {

    private static final List<ToolDefinition> BUILT_IN_TOOLS = List.of(
        ToolDefinition.builder()
            .id("web_search").name("Web Search")
            .description("Search the web for real-time information using Google/Bing")
            .category("Search").icon("🔍").build(),
        ToolDefinition.builder()
            .id("url_reader").name("URL Reader")
            .description("Fetch and extract text content from a web page URL")
            .category("Search").icon("🌐").build(),
        ToolDefinition.builder()
            .id("knowledge_base_search").name("Knowledge Base Search")
            .description("Search the organization's internal knowledge base using vector similarity")
            .category("Search").icon("📚").build(),
        ToolDefinition.builder()
            .id("code_executor").name("Code Executor")
            .description("Execute Python or JavaScript code in a sandboxed environment")
            .category("Code").icon("💻").build(),
        ToolDefinition.builder()
            .id("code_analyzer").name("Code Analyzer")
            .description("Analyze source code for patterns, bugs, and improvements")
            .category("Code").icon("🔬").build(),
        ToolDefinition.builder()
            .id("file_reader").name("File Reader")
            .description("Read and parse files (PDF, DOCX, CSV, JSON, TXT)")
            .category("Data").icon("📄").build(),
        ToolDefinition.builder()
            .id("file_writer").name("File Writer")
            .description("Write content to files in various formats")
            .category("Data").icon("✏️").build(),
        ToolDefinition.builder()
            .id("calculator").name("Calculator")
            .description("Perform mathematical calculations and unit conversions")
            .category("Utility").icon("🧮").build(),
        ToolDefinition.builder()
            .id("json_transformer").name("JSON Transformer")
            .description("Transform, filter, and reshape JSON data using JMESPath expressions")
            .category("Data").icon("🔄").build(),
        ToolDefinition.builder()
            .id("database_query").name("Database Query")
            .description("Execute read-only SQL queries against configured databases")
            .category("Data").icon("🗄️").build(),
        ToolDefinition.builder()
            .id("rest_api_call").name("REST API Call")
            .description("Make HTTP requests to external REST APIs with configurable method, headers, and body")
            .category("Integration").icon("🔗").build(),
        ToolDefinition.builder()
            .id("email_sender").name("Email Sender")
            .description("Send emails via configured SMTP or API (SendGrid, SES)")
            .category("Communication").icon("📧").build(),
        ToolDefinition.builder()
            .id("slack_notifier").name("Slack Notifier")
            .description("Send messages and notifications to Slack channels")
            .category("Communication").icon("💬").build(),
        ToolDefinition.builder()
            .id("image_analyzer").name("Image Analyzer")
            .description("Analyze images using vision models — describe, OCR, classify")
            .category("AI").icon("🖼️").build(),
        ToolDefinition.builder()
            .id("text_summarizer").name("Text Summarizer")
            .description("Summarize long text documents into concise summaries")
            .category("AI").icon("📝").build(),
        ToolDefinition.builder()
            .id("sentiment_analyzer").name("Sentiment Analyzer")
            .description("Analyze text sentiment — positive, negative, neutral with confidence score")
            .category("AI").icon("😊").build(),
        ToolDefinition.builder()
            .id("translation").name("Translation")
            .description("Translate text between languages using LLM or Google Translate")
            .category("AI").icon("🌍").build(),
        ToolDefinition.builder()
            .id("data_validator").name("Data Validator")
            .description("Validate data against JSON Schema or custom rules")
            .category("Utility").icon("✅").build()
    );

    @Override
    public Flux<ToolDefinition> listAvailableTools() {
        return Flux.fromIterable(BUILT_IN_TOOLS);
    }
}
