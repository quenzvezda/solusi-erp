package com.solusi.erp.purchasing.purchaseorder.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PurchaseOrderLineResponse {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal receivedQuantity;
    private Long uomId;
    private String uomName;
    private BigDecimal unitPrice;
    private BigDecimal taxRate;
    private BigDecimal lineSubtotal;
    private BigDecimal lineTax;
    private BigDecimal lineTotal;
    private Long prLineId;
    private String note;
}
