package com.solusi.erp.purchasing.purchaserequisition.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PurchaseRequisitionLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal quantity;
    private Long uomId;
    private String uomName;
    private LocalDate requiredDate;
    private BigDecimal estimatedUnitPrice;
    private Long suggestedSupplierId;
    private String supplierName;
    private String note;
}
