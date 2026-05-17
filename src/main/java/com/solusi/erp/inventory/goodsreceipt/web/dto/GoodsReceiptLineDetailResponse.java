package com.solusi.erp.inventory.goodsreceipt.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class GoodsReceiptLineDetailResponse {
    private Long id;
    private Long referenceLineId;
    private Long productId;
    private String productCode;
    private String productName;
    private Boolean serialized;
    private BigDecimal quantityReceived;
    private BigDecimal baseQuantity;
    private Long uomId;
    private String uomCode;
    private Long containerId;
    private String containerCode;
    private BigDecimal unitPrice;
    private BigDecimal inventoryAmount;
    private BigDecimal taxAmount;
    private BigDecimal grIrAmount;
    private BigDecimal billedQuantity;
    private String billingStatus;
    private String serialNumber;

    @JsonProperty("poLineId")
    public Long getPoLineId() {
        return referenceLineId;
    }

    public String getSerialDisplay() {
        if (serialNumber == null || serialNumber.isBlank()) {
            return null;
        }
        return Arrays.stream(serialNumber.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .map(value -> "(" + value + ")")
            .collect(Collectors.joining(", "));
    }
}
