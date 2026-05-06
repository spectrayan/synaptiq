package com.synaptiq.catalog.application.service;

import com.synaptiq.catalog.application.port.in.CatalogCommandUseCase;
import com.synaptiq.catalog.application.port.out.CatalogPersistencePort;
import com.synaptiq.catalog.domain.model.CatalogItem;
import com.synaptiq.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogCommandService implements CatalogCommandUseCase {
    private final CatalogPersistencePort persistence;

    @Override public Mono<CatalogItem> createItem(CreateItemCommand cmd) {
        var item = CatalogItem.builder().tenantId(cmd.tenantId()).data(cmd.data()).status(cmd.status()).build();
        return persistence.saveItem(item);
    }

    @Override public Mono<CatalogItem> updateItem(String tenantId, String itemId, Map<String, Object> data) {
        return persistence.findItemById(itemId, tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Item not found")))
            .flatMap(item -> {
                item.getData().putAll(data);
                item.setUpdatedAt(Instant.now());
                return persistence.saveItem(item);
            });
    }

    @Override public Mono<Void> deleteItem(String tenantId, String itemId) {
        return persistence.findItemById(itemId, tenantId)
            .switchIfEmpty(Mono.error(new ResourceNotFoundException("Item not found")))
            .flatMap(item -> {
                item.setStatus("archived");
                return persistence.saveItem(item);
            }).then();
    }

    @Override
    public Mono<com.synaptiq.infrastructure.in.web.dto.CatalogImportResponse> importItems(String tenantId, org.springframework.http.codec.multipart.FilePart filePart) {
        return org.springframework.core.io.buffer.DataBufferUtils.join(filePart.content())
            .map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                org.springframework.core.io.buffer.DataBufferUtils.release(dataBuffer);
                return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            })
            .flatMapIterable(content -> {
                try {
                    var parser = org.apache.commons.csv.CSVParser.parse(content, org.apache.commons.csv.CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build());
                    return parser.getRecords();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse CSV", e);
                }
            })
            .flatMap(record -> {
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                record.toMap().forEach((k, v) -> data.put(k, v));
                return createItem(new CreateItemCommand(tenantId, data, "ACTIVE"));
            })
            .count()
            .map(count -> new com.synaptiq.infrastructure.in.web.dto.CatalogImportResponse().imported(count.intValue()).skipped(0));
    }
}
