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

    @NotNull(message = "{validation.notnull}")
    private Long containerId;

    @NotNull(message = "{validation.notnull}")
    private BigDecimal quantity;

    @NotNull(message = "{validation.notnull}")
    private BigDecimal unitCost;

    private String serialNumber;
}
