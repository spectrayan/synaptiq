package com.spectrayan.synaptiq.knowledgebase.infrastructure.web;

import com.spectrayan.synaptiq.infrastructure.in.web.api.KnowledgeBaseApi;
import com.spectrayan.synaptiq.infrastructure.in.web.dto.*;
import com.spectrayan.synaptiq.knowledgebase.application.port.in.DocumentUploadUseCase;
import com.spectrayan.synaptiq.knowledgebase.application.port.in.KnowledgeSearchUseCase;
import com.spectrayan.synaptiq.knowledgebase.application.port.out.KnowledgeCategoryPersistencePort;
import com.spectrayan.synaptiq.knowledgebase.application.port.out.KnowledgeDocumentPersistencePort;
import com.spectrayan.synaptiq.knowledgebase.application.service.KnowledgeCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KnowledgeBaseController implements KnowledgeBaseApi {

    private final DocumentUploadUseCase documentUploadUseCase;
    private final KnowledgeSearchUseCase knowledgeSearchUseCase;
    private final KnowledgeCategoryService categoryService;
    private final KnowledgeDocumentPersistencePort documentPersistencePort;
    private final KnowledgeCategoryPersistencePort categoryPersistencePort;

    @Override
    public Mono<ResponseEntity<KnowledgeCategoryResponse>> createKnowledgeCategory(
            Mono<CreateCategoryRequest> createCategoryRequest,
            @Nullable String xTenantID,
            ServerWebExchange exchange) {
        String tenantId = resolveTenantId(xTenantID, exchange);
        return createCategoryRequest.flatMap(req ->
                categoryService.createCategory(tenantId, req.getName(), req.getDescription())
                        .map(cat -> ResponseEntity.ok(new KnowledgeCategoryResponse(cat.getId(), cat.getName())
                                .description(cat.getDescription()))));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteKnowledgeDocument(
            String docId,
            @Nullable String xTenantID,
            ServerWebExchange exchange) {
        return documentPersistencePort.deleteById(docId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<KnowledgeBaseStatusResponse>> getKnowledgeBaseStatus(
            @Nullable String xTenantID,
            ServerWebExchange exchange) {
        String tenantId = resolveTenantId(xTenantID, exchange);
        return Mono.zip(
                documentPersistencePort.findByTenantId(tenantId).count(),
                categoryService.listCategories(tenantId)
                    .map(cat -> new KnowledgeCategoryResponse(cat.getId(), cat.getName())
                        .description(cat.getDescription()))
                    .collectList()
        ).map(tuple -> {
            var categories = tuple.getT2();
            return ResponseEntity.ok(new KnowledgeBaseStatusResponse()
                .totalDocuments(tuple.getT1().intValue())
                .totalCategories(categories.size())
                .totalStorageBytes(0L)
                .categories(categories));
        });
    }

    @Override
    public Mono<ResponseEntity<KnowledgeDocumentResponse>> getKnowledgeDocument(
            String docId,
            @Nullable String xTenantID,
            ServerWebExchange exchange) {
        return documentPersistencePort.findById(docId)
                .map(doc -> ResponseEntity.ok(new KnowledgeDocumentResponse(
                        doc.getId(),
                        doc.getFileName(),
                        KnowledgeDocumentResponse.StatusEnum.fromValue(doc.getStatus().name()),
                        KnowledgeDocumentResponse.SourceTypeEnum.fromValue(doc.getSourceType().name()))
                        .categoryId(doc.getCategoryId())
                        .tags(doc.getTags())
                        .sizeBytes(doc.getSizeBytes())))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<DocumentListResponse>> listKnowledgeDocuments(
            @Nullable String xTenantID,
            @Nullable String categoryId,
            @Nullable List<String> tags,
            ServerWebExchange exchange) {
        String tenantId = resolveTenantId(xTenantID, exchange);
        return documentPersistencePort.findByTenantId(tenantId)
                .map(doc -> new KnowledgeDocumentResponse(
                        doc.getId(),
                        doc.getFileName(),
                        KnowledgeDocumentResponse.StatusEnum.fromValue(doc.getStatus().name()),
                        KnowledgeDocumentResponse.SourceTypeEnum.fromValue(doc.getSourceType().name()))
                        .categoryId(doc.getCategoryId())
                        .tags(doc.getTags())
                        .sizeBytes(doc.getSizeBytes()))
                .collectList()
                .map(docs -> ResponseEntity.ok(new DocumentListResponse().documents(docs)));
    }

    @Override
    public Mono<ResponseEntity<KnowledgeSearchResponse>> searchKnowledgeBase(
            Mono<KnowledgeSearchRequest> knowledgeSearchRequest,
            @Nullable String xTenantID,
            ServerWebExchange exchange) {
        String tenantId = resolveTenantId(xTenantID, exchange);
        return knowledgeSearchRequest.flatMap(req ->
                knowledgeSearchUseCase.search(
                        tenantId,
                        req.getQuery(),
                        req.getCategoryId(),
                        req.getTags(),
                        req.getTopK() != null ? req.getTopK() : 5
                ).map(chunk -> new DocumentChunkResponse(
                        chunk.getDocumentId(),
                        chunk.getContent(),
                        chunk.getSimilarityScore() != null ? chunk.getSimilarityScore() : 0.0))
                .collectList()
                .map(results -> ResponseEntity.ok(new KnowledgeSearchResponse().results(results))));
    }

    @Override
    public Mono<ResponseEntity<KnowledgeDocumentResponse>> uploadKnowledgeDocument(
            @Nullable String xTenantID,
            @Nullable List<String> tags,
            Part file,
            String categoryId,
            ServerWebExchange exchange) {
        String tenantId = resolveTenantId(xTenantID, exchange);
        
        if (!(file instanceof FilePart filePart)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        String fileName = filePart.filename();
        List<String> resolvedTags = tags != null ? tags : List.of();
        
        return DataBufferUtils.join(filePart.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new org.springframework.core.io.ByteArrayResource(bytes);
                })
                .cast(org.springframework.core.io.Resource.class)
                .flatMap(resource -> documentUploadUseCase.ingestDocument(tenantId, categoryId, resolvedTags, resource, fileName))
                .flatMap(docId -> documentPersistencePort.findById(docId))
                .map(doc -> ResponseEntity.ok(new KnowledgeDocumentResponse(
                        doc.getId(),
                        doc.getFileName(),
                        KnowledgeDocumentResponse.StatusEnum.fromValue(doc.getStatus().name()),
                        KnowledgeDocumentResponse.SourceTypeEnum.fromValue(doc.getSourceType().name()))
                        .categoryId(doc.getCategoryId())
                        .tags(doc.getTags())
                        .sizeBytes(doc.getSizeBytes())));
    }

    private String resolveTenantId(@Nullable String xTenantID, ServerWebExchange exchange) {
        return xTenantID != null ? xTenantID : "default";
    }
}
