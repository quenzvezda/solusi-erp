package com.solusi.erp.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for Stock Adjustment Lines.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentLineRequest {

    private Long id;
    private Integer version;

    @NotNull(message = "{validation.notnull}")
    private Long productId;
    
    private String productCode; // For display in Edit mode
    private String productName; // For display in Edit mode
    private String uomName;     // For display in UI

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
