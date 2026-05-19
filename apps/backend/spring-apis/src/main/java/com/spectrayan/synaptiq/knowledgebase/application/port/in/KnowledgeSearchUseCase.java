package com.spectrayan.synaptiq.knowledgebase.application.port.in;

import com.spectrayan.synaptiq.knowledgebase.domain.model.DocumentChunk;
import reactor.core.publisher.Flux;

import java.util.List;

public interface KnowledgeSearchUseCase {
    Flux<DocumentChunk> search(String tenantId, String query, String categoryId, List<String> tags, int topK);
}
