package com.solusi.erp.accountspayable.vendorbill.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VendorBillLineRequest {
    private Long id;
    @NotNull
    private Long grLineId;
    @NotNull
    private Long productId;
    private String productName;
    private String description;
    @NotNull
    private BigDecimal qtyBilled;
    @NotNull
    private Long uomId;
    private String uomName;
    private BigDecimal unitPrice;
    private BigDecimal inventoryAmount;
    private BigDecimal taxAmount;
}
