package com.solusi.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Stock Adjustment Lines.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentLineResponse {
    private Long id;
    private Integer version;
    private Long productId;
    private String productCode;
    private String productName;

    private Long gridId;
    private String gridCode;
    private String gridName;
    
    private Long containerId;
    private String containerCode;
    private String containerName;
    private String facilityName;
    
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalAmount;
    private String serialNumber;
}
