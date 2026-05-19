package com.spectrayan.synaptiq.knowledgebase.application.port.in;

import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;
import java.util.List;

public interface DocumentUploadUseCase {
    Mono<String> ingestDocument(String tenantId, String categoryId, List<String> tags, Resource resource, String fileName);
}
