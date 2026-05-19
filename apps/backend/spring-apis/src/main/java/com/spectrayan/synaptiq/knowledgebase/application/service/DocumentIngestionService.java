package com.spectrayan.synaptiq.knowledgebase.application.service;

import com.spectrayan.synaptiq.knowledgebase.application.port.in.DocumentUploadUseCase;
import com.spectrayan.synaptiq.knowledgebase.application.port.out.KnowledgeDocumentPersistencePort;
import com.spectrayan.synaptiq.knowledgebase.domain.model.DocumentSourceType;
import com.spectrayan.synaptiq.knowledgebase.domain.model.KnowledgeDocument;
import com.spectrayan.synaptiq.knowledgebase.infrastructure.reader.TikaReaderAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService implements DocumentUploadUseCase {

    private final TikaReaderAdapter tikaReaderAdapter;
    private final VectorStore vectorStore;
    private final KnowledgeDocumentPersistencePort persistencePort;
    
    @Override
    public Mono<String> ingestDocument(String tenantId, String categoryId, List<String> tags, Resource resource, String fileName) {
        log.info("Starting ingestion for file: {} (tenant: {})", fileName, tenantId);
        
        KnowledgeDocument initialDocMeta = KnowledgeDocument.builder()
                .tenantId(tenantId)
                .categoryId(categoryId)
                .tags(tags != null ? tags : List.of())
                .fileName(fileName)
                .sourceType(DocumentSourceType.FILE_UPLOAD)
                .build();
                
        return persistencePort.save(initialDocMeta)
                .flatMap(docMeta -> persistencePort.save(docMeta.markProcessing())
                        .flatMap(processingDoc -> processAndStore(processingDoc, resource))
                        .flatMap(readyDoc -> persistencePort.save(readyDoc.markReady()))
                        .onErrorResume(e -> {
                            log.error("Failed to ingest document: {}", fileName, e);
                            return persistencePort.save(docMeta.markFailed())
                                    .then(Mono.error(new RuntimeException("Document ingestion failed", e)));
                        }))
                .map(KnowledgeDocument::getId)
                .doOnSuccess(id -> log.info("Successfully ingested document: {}", fileName));
    }
    
    private Mono<KnowledgeDocument> processAndStore(KnowledgeDocument docMeta, Resource resource) {
        return Mono.fromCallable(() -> {
            // 1. Extract text using Tika
            List<Document> extractedDocs = tikaReaderAdapter.extractText(resource);
            
            // 2. Add metadata for tenant isolation and filtering
            final String docId = docMeta.getId();
            final String tenantId = docMeta.getTenantId();
            final String categoryId = docMeta.getCategoryId();
            final List<String> tags = docMeta.getTags();
            
            extractedDocs.forEach(doc -> {
                doc.getMetadata().put("tenantId", tenantId);
                doc.getMetadata().put("documentId", docId);
                if (categoryId != null && !categoryId.isEmpty()) {
                    doc.getMetadata().put("categoryId", categoryId);
                }
                if (tags != null && !tags.isEmpty()) {
                    doc.getMetadata().put("tags", tags);
                }
            });
            
            // 3. Chunk the text
            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    .withChunkSize(800)          // Max tokens per chunk
                    .withMinChunkSizeChars(350)  // Min characters per chunk
                    .withKeepSeparator(true)
                    .build();
            List<Document> chunkedDocs = splitter.apply(extractedDocs);
            
            log.info("Document '{}' split into {} chunks, storing in {} (class: {})",
                    docMeta.getFileName(), chunkedDocs.size(),
                    vectorStore.getName(), vectorStore.getClass().getSimpleName());
            
            // 4. Store in Vector Store (generates embeddings automatically)
            vectorStore.add(chunkedDocs);
            
            log.info("VectorStore.add() completed for '{}' — {} chunks stored",
                    docMeta.getFileName(), chunkedDocs.size());
            
            return docMeta;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
