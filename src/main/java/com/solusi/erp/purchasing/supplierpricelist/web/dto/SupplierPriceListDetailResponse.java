package com.solusi.erp.purchasing.supplierpricelist.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SupplierPriceListDetailResponse extends BaseAuditResponse {
    private String code;
    private Long supplierId;
    private String supplierName;
    private Long productId;
    private String productName;
    private Long uomId;
    private String uomName;
    private Long currencyId;
    private String currencyName;
    private BigDecimal unitPrice;
    private BigDecimal minQuantity;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String note;
    private boolean active;
}
