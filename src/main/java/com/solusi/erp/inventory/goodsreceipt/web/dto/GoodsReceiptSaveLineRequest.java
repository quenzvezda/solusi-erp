package com.solusi.erp.inventory.goodsreceipt.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Arrays;

@Data
@NoArgsConstructor
public class GoodsReceiptSaveLineRequest {
    private Long id;
    @JsonAlias("poLineId")
    private Long referenceLineId;
    private Long productId;
    private String productCode;
    private String productName;
    private Boolean serialized;
    private BigDecimal quantityReceived;
    private BigDecimal orderedQuantity;
    private BigDecimal receivedToDateQuantity;
    private BigDecimal remainingQuantity;
    private Long uomId;
    private String uomCode;
    private String uomName;
    private BigDecimal unitPrice;
    @NotNull(message = "{label.container} {validation.notnull.suffix}")
    private Long containerId;
    private String containerCode;
    private String containerName;
    private String serialNumber;

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
