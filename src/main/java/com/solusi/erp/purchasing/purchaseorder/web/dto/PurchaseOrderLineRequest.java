package com.solusi.erp.purchasing.purchaseorder.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PurchaseOrderLineRequest {
    private Long productId;
    private BigDecimal quantity;
    private Long uomId;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private Long prLineId;
    private String note;
    // Display names
    private String productName;
    private String uomName;
}
