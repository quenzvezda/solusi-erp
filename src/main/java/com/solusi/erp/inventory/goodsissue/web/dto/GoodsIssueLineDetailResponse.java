package com.solusi.erp.inventory.goodsissue.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class GoodsIssueLineDetailResponse {
    private Long id;
    private Long referenceLineId;
    private Long productId;
    private String productCode;
    private String productName;
    private Boolean serialized;
    private BigDecimal quantityIssued;
    private BigDecimal baseQuantity;
    private Long uomId;
    private String uomCode;
    private Long facilityId;
    private String facilityCode;
    private String facilityName;
    private Long gridId;
    private String gridCode;
    private String gridName;
    private Long containerId;
    private String containerCode;
    private String containerName;
    private String serialNumber;
    private BigDecimal unitCost;
    private BigDecimal inventoryAmount;
    private BigDecimal taxBaseAmount;
    private BigDecimal taxAmount;
    private BigDecimal clearingAmount;
    private String valuationRefType;
    private Long valuationRefId;
    private Long valuationRefLineId;

    public String getSerialDisplay() {
        if (serialNumber == null || serialNumber.isBlank()) {
            return null;
        }
        return Arrays.stream(serialNumber.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.joining(", "));
    }
}
