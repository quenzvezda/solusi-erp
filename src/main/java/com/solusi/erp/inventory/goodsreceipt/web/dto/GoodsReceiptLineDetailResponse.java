package com.solusi.erp.inventory.goodsreceipt.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private String serialNumber;

    @JsonProperty("poLineId")
    public Long getPoLineId() {
        return referenceLineId;
    }
}
