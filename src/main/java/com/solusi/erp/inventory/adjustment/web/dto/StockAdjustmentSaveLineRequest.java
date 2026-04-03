package com.solusi.erp.inventory.adjustment.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Save/Edit line request DTO for StockAdjustment lines.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentSaveLineRequest {

    private Long id;
    private Integer version;

    @NotNull(message = "{label.product} {validation.notnull.suffix}")
    private Long productId;

    private String productCode;
    private String productName;
    private String uomName;
    private Boolean isSerialized;

    private Long gridId;
    private String gridCode;
    private String gridName;

    @NotNull(message = "{label.container} {validation.notnull.suffix}")
    private Long containerId;

    private String containerCode;
    private String containerName;
    private String facilityName;

    private Long uomId;
    private BigDecimal conversionFactor;

    @NotNull(message = "{label.qty} {validation.notnull.suffix}")
    private BigDecimal quantity;

    @NotNull(message = "{label.price} {validation.notnull.suffix}")
    private BigDecimal unitCost;

    private String serialNumber;
}
