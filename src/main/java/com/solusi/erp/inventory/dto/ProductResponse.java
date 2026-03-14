package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO for displaying Product data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductResponse extends BaseAuditResponse {
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
    
    private Long brandId;
    private String brandName;
    
    private String hscode;
    private Boolean isActive;
    private Boolean isSerialized;
    
    private BigDecimal minStock;
    private BigDecimal maxStock;
    
    private BigDecimal weightNet;
    private BigDecimal weightGross;
    private Long weightUomId;
    private String weightUomCode;
    
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Long dimensionUomId;
    private String dimensionUomCode;
}

