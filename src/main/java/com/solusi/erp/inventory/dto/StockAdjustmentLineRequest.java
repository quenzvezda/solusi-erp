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

    @NotNull(message = "{validation.notnull}")
    private Long productId;

    @NotNull(message = "{validation.notnull}")
    private Long containerId;

    @NotNull(message = "{validation.notnull}")
    private BigDecimal quantity;

    @NotNull(message = "{validation.notnull}")
    private BigDecimal unitCost;

    private String serialNumber;
}
