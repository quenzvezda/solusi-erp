package com.solusi.erp.inventory.goodsissue.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;

@Data
@NoArgsConstructor
public class GoodsIssueSaveLineRequest {
    private Long id;
    private Long referenceLineId;
    @NotNull(message = "{label.product} {validation.notnull.suffix}")
    private Long productId;
    private String productCode;
    private String productName;
    private Boolean serialized;
    @NotNull(message = "{label.qty.issued} {validation.notnull.suffix}")
    private BigDecimal quantityIssued;
    private Long uomId;
    private String uomCode;
    private String uomName;
    private BigDecimal baseQuantity;
    @NotNull(message = "{label.gi.facility} {validation.notnull.suffix}")
    private Long facilityId;
    private String facilityCode;
    private String facilityName;
    @NotNull(message = "{label.grid} {validation.notnull.suffix}")
    private Long gridId;
    private String gridCode;
    private String gridName;
    @NotNull(message = "{label.container} {validation.notnull.suffix}")
    private Long containerId;
    private String containerCode;
    private String containerName;
    private String serialNumber;
    private BigDecimal unitCost;
    private String valuationRefType;
    private Long valuationRefId;
    private Long valuationRefLineId;

    public long getSerialDraftCount() {
        if (serialNumber == null || serialNumber.isBlank()) {
            return 0;
        }
        return Arrays.stream(serialNumber.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .count();
    }
}
