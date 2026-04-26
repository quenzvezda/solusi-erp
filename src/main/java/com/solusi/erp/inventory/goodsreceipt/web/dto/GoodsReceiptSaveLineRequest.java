package com.solusi.erp.inventory.goodsreceipt.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class GoodsReceiptSaveLineRequest {
    private Long id;
    private Long poLineId;
    private Long productId;
    private Boolean serialized;
    private BigDecimal quantityReceived;
    private Long uomId;
    private Long containerId;
    private String serialNumber;
}
