package com.solusi.erp.inventory.adjustment.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Detail response DTO for a single StockAdjustment line.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentLineDetailResponse {
    private Long id;
    private Integer version;
    private Long productId;
    private String productCode;
    private String productName;
    private Boolean isSerialized;
    private Long gridId;
    private String gridCode;
    private String gridName;
    private Long containerId;
    private String containerCode;
    private String containerName;
    private String facilityName;
    private Long uomId;
    private String uomName;
    private BigDecimal conversionFactor;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalAmount;
    private String serialNumber;
}
