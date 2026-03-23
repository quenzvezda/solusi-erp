package com.solusi.erp.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

    @NotNull(message = "{label.product} {validation.notnull.suffix}")
    private Long productId;
    
    private String productCode; // For display in Edit mode
    private String productName; // For display in Edit mode
    private String uomName;     // For display in UI
    private Boolean isSerialized; // For UI logic in Edit mode

    @NotNull(message = "{label.grid} {validation.notnull.suffix}")
    private Long gridId;
    
    private String gridCode; // For display in Edit mode
    private String gridName; // For display in Edit mode

    @NotNull(message = "{label.container} {validation.notnull.suffix}")
    private Long containerId;
    
    private String containerCode; // For display in Edit mode
    private String containerName; // For display in Edit mode

    private Long uomId;
    private BigDecimal conversionFactor;

    @NotNull(message = "{label.qty} {validation.notnull.suffix}")
    @Positive(message = "{label.qty} {validation.positive.suffix}")
    private BigDecimal quantity;

    @NotNull(message = "{label.price} {validation.notnull.suffix}")
    @Positive(message = "{label.price} {validation.positive.suffix}")
    private BigDecimal unitCost;

    private String serialNumber;
}
