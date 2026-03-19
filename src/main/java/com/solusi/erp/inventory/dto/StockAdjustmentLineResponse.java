package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Stock Adjustment Lines.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentLineResponse extends BaseAuditResponse {
    private Long productId;
    private String productCode;
    private String productName;

    private Long gridId;
    private String gridCode;
    
    private Long containerId;
    private String containerCode;
    private String facilityName;
    
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalAmount;
    private String serialNumber;
}
