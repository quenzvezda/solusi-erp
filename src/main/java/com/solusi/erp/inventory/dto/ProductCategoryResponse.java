package com.solusi.erp.inventory.dto;

import com.solusi.erp.inventory.model.ProductCategoryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for displaying Product Category data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryResponse {
    private Long id;
    private String code;
    private String name;
    private ProductCategoryType type;
    private String note;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private Integer version;
}
