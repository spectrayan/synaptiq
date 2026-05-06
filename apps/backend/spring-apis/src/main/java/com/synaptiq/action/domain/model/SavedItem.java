package com.synaptiq.action.domain.model;

import com.synaptiq.shared.domain.AggregateRoot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Domain entity for a saved/bookmarked item.
 * Pure POJO — no framework annotations.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SavedItem extends AggregateRoot {

    private String tenantId;
    private String appId;
    private String itemId;
    private String sessionId;
    private String userUid;
    private ItemSnapshot itemSnapshot;

    /**
     * Typed snapshot of a saved item — replaces raw Map.
     * Contains all display-relevant fields captured at save time.
     */
    @Getter @Setter @Builder @NoArgsConstructor @lombok.AllArgsConstructor
    public static class ItemSnapshot {
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
