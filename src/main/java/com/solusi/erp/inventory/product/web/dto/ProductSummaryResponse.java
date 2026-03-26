package com.solusi.erp.inventory.product.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Intent-Based DTO for Product Summary (Listing).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductSummaryResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private String barcode;
    private String note;
    
    private Long categoryId;
    private String categoryName;
    private String categoryType;
    
    private Long uomId;
    private String uomName;
    private String uomCode;
    
    private String brandName;
    
    private Boolean isActive;
    private Boolean isSerialized;
}
