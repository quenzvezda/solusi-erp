package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for Stock Adjustment Lines.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockAdjustmentLineRequest extends BaseAuditResponse {

    @NotNull(message = "{validation.notnull}")
    private Long productId;
    
    private String productCode; // For display in Edit mode
    private String productName; // For display in Edit mode

    @NotNull(message = "{validation.notnull}")
    private Long gridId;
    
    private String gridCode; // For display in Edit mode
    private String gridName; // For display in Edit mode

    @NotNull(message = "{validation.notnull}")
    private Long containerId;
    
    private String containerCode; // For display in Edit mode
    private String containerName; // For display in Edit mode

    @NotNull(message = "{validation.notnull}")
    private BigDecimal quantity;

    @NotNull(message = "{validation.notnull}")
    private BigDecimal unitCost;

    private String serialNumber;
}
