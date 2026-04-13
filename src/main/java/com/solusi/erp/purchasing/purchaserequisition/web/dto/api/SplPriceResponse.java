package com.solusi.erp.purchasing.purchaserequisition.web.dto.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SplPriceResponse {
    private Long splId;
    private String splCode;
    private BigDecimal unitPrice;
    private Long supplierId;
    private Long productId;
    private Long uomId;
    private Long currencyId;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private boolean active;
}
