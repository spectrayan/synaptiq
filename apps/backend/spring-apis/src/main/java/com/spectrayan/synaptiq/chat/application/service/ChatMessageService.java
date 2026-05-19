package com.spectrayan.synaptiq.chat.application.service;

import com.spectrayan.synaptiq.chat.application.port.in.ChatMessageUseCase;
import com.spectrayan.synaptiq.chat.infrastructure.tools.RbacAdminTools;
import com.spectrayan.synaptiq.shared.event.TokenUsageEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;

@Service
public class ChatMessageService implements ChatMessageUseCase {
    private final ChatClient chatClient;
    private final ApplicationEventPublisher eventPublisher;
    private final RbacAdminTools rbacAdminTools;
    private final VectorStore vectorStore;

    public ChatMessageService(ChatClient.Builder chatClientBuilder,
                              ApplicationEventPublisher eventPublisher,
                              RbacAdminTools rbacAdminTools,
                              VectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.eventPublisher = eventPublisher;
        this.rbacAdminTools = rbacAdminTools;
        this.vectorStore = vectorStore;
    }

    @Override
    public Flux<String> streamMessage(String tenantId, String sessionId, String message, String modelOverride) {
        return streamMessage(tenantId, sessionId, message, modelOverride, null);
    }

    @Override
    public Flux<String> streamMessage(String tenantId, String sessionId, String message,
                                       String modelOverride, List<String> knowledgeBaseIds) {

        // Build filter expression with tenant isolation + optional KB category scoping
        String filterExpression = buildFilterExpression(tenantId, knowledgeBaseIds);

        SearchRequest searchRequest = SearchRequest.builder()
                .filterExpression(filterExpression)
                .topK(5)
                .build();
                
        // Manual LLM Grounding (equivalent to RetrievalAugmentationAdvisor which is missing in this version)
        String context = vectorStore.similaritySearch(searchRequest)
                .stream()
                .map(doc -> doc.getText())
                .collect(java.util.stream.Collectors.joining("\n\n---\n\n"));
                
        String systemPrompt = """
                You are a helpful assistant. Use the following contextual information to answer the user's questions.
                If the answer is not contained within the context, say so.
                
                Context:
                {context}
                """;

        return chatClient.prompt()
                .system(sys -> sys.text(systemPrompt).param("context", context))
                .user(message)
                .tools(rbacAdminTools)
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    if (response.getMetadata() != null && response.getMetadata().getUsage() != null) {
                        var usage = response.getMetadata().getUsage();
                        if (usage.getPromptTokens() > 0 || usage.getCompletionTokens() > 0) {
                            eventPublisher.publishEvent(new TokenUsageEvent(
                                tenantId, sessionId,
                                "vertex-gemini",
                                usage.getPromptTokens(),
                                usage.getCompletionTokens(),
                                Instant.now()
                            ));
                        }
                    }
                })
                .map(response -> {
                    if (response.getResult() != null && response.getResult().getOutput() != null && response.getResult().getOutput().getText() != null) {
                        return response.getResult().getOutput().getText();
                    }
                    return "";
                })
                .filter(content -> !content.isEmpty());
    }

    /**
     * Build a Spring AI filter expression for the vector store search.
     * <p>
     * Always includes tenant isolation. When knowledgeBaseIds are provided,
     * additionally scopes the search to only those KB categories.
     *
     * @param tenantId         the tenant identifier (required)
     * @param knowledgeBaseIds optional list of KB category IDs
     * @return filter expression string for {@link SearchRequest}
     */
    private String buildFilterExpression(String tenantId, List<String> knowledgeBaseIds) {
        StringBuilder filter = new StringBuilder();
        filter.append(String.format("tenantId == '%s'", tenantId));

        if (knowledgeBaseIds != null && !knowledgeBaseIds.isEmpty()) {
            if (knowledgeBaseIds.size() == 1) {
                filter.append(String.format(" && categoryId == '%s'", knowledgeBaseIds.get(0)));
            } else {
                // Build an OR clause: categoryId == 'a' || categoryId == 'b' || ...
                String categoryFilter = knowledgeBaseIds.stream()
                        .map(id -> String.format("categoryId == '%s'", id))
                        .collect(java.util.stream.Collectors.joining(" || "));
                filter.append(" && (").append(categoryFilter).append(")");
            }
        }

        return filter.toString();
    }
}
