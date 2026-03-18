package com.solusi.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Specialized Lookup DTO for Inventory entities that need parent context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLookupDto {
    private Long id;
    private String name;
    private String subText;
    private Long parentId;   // e.g. gridId for Container, facilityId for Grid
    private String parentName; // e.g. gridName for Container
}
