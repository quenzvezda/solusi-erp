package com.solusi.erp.accountspayable.vendorbill.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VendorBillLineResponse {
    private Long id;
    private Long grLineId;
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal qtyBilled;
    private Long uomId;
    private String uomName;
    private BigDecimal unitPrice;
    private BigDecimal inventoryAmount;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
}
