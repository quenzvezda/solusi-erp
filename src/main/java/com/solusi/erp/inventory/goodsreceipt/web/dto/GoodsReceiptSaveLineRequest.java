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
    private String productCode;
    private String productName;
    private Boolean serialized;
    private BigDecimal quantityReceived;
    private Long uomId;
    private String uomCode;
    private String uomName;
    private Long containerId;
    private String containerCode;
    private String containerName;
    private String serialNumber;
}
