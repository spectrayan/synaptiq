package com.spectrayan.synaptiq.knowledgebase.application.service;

import com.spectrayan.synaptiq.knowledgebase.application.port.in.KnowledgeSearchUseCase;
import com.spectrayan.synaptiq.knowledgebase.domain.model.DocumentChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSearchService implements KnowledgeSearchUseCase {

    private final VectorStore vectorStore;

    @Override
    public Flux<DocumentChunk> search(String tenantId, String query, String categoryId, List<String> tags, int topK) {
        return Flux.defer(() -> {
            log.debug("Searching knowledge base for tenant: {}, query: '{}'", tenantId, query);
            
            StringBuilder filterStr = new StringBuilder(String.format("tenantId == '%s'", tenantId));
            
            if (categoryId != null && !categoryId.isBlank()) {
                filterStr.append(String.format(" && categoryId == '%s'", categoryId));
            }
            
            if (tags != null && !tags.isEmpty()) {
                String tagsArr = tags.stream().map(t -> "'" + t + "'").collect(java.util.stream.Collectors.joining(","));
                filterStr.append(String.format(" && tags in [%s]", tagsArr));
            }
            
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .filterExpression(filterStr.toString())
                    .build();
            
            List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch(searchRequest);
            return Flux.fromIterable(results)
                    .map(doc -> DocumentChunk.builder()
                            .id(doc.getId())
                            .documentId((String) doc.getMetadata().get("documentId"))
                            .tenantId((String) doc.getMetadata().get("tenantId"))
                            .categoryId((String) doc.getMetadata().get("categoryId"))
                            .content(doc.getText()) // or getFormattedContent() if getText() fails
                            .similarityScore(getSimilarityScore(doc))
                            .build());
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private Double getSimilarityScore(org.springframework.ai.document.Document doc) {
        // Spring AI VectorStore implementation dependent metadata key
        Object distance = doc.getMetadata().get("distance");
        if (distance instanceof Number num) {
            // Some stores return distance where lower is better, others return similarity.
            return num.doubleValue();
        }
        return null;
    }
}
