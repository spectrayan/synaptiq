package com.spectrayan.synaptiq.action.infrastructure.persistence.mongo.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document for the SavedItem aggregate.
 * Framework annotations live HERE — never on domain models.
 */
@Data
@Builder
@Document(collection = "saved_items")
public class SavedItemDocument {

    @Id
    private String id;
    private String tenantId;
    private String appId;
    private String itemId;
    private String sessionId;
    private String userUid;
    private ItemSnapshotEmbed itemSnapshot;
    @CreatedDate
    private Instant createdAt;

    @Data @Builder @lombok.NoArgsConstructor @lombok.AllArgsConstructor
    public static class ItemSnapshotEmbed {
        private String title;
        private String subtitle;
        private String imageUrl;
        private String description;
        private String price;
        private String category;
        private String dataSourceId;
        private String sourceItemId;
    }
}
