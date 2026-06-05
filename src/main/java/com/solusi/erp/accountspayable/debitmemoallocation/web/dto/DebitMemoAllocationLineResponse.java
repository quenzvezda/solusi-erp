package com.solusi.erp.accountspayable.debitmemoallocation.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DebitMemoAllocationLineResponse {
    private Long id;
    private Long vendorBillId;
    private String vendorBillCode;
    private BigDecimal debitMemoRemainingAtDraft;
    private BigDecimal vendorBillOutstandingAtDraft;
    private BigDecimal appliedGrossOriginal;
    private BigDecimal appliedDppOriginal;
    private BigDecimal appliedTaxOriginal;
    private BigDecimal grirReversalBase;
    private BigDecimal taxReversalBase;
    private BigDecimal vendorBillExchangeRate;
    private BigDecimal apReductionBase;
    private BigDecimal fxLossBase;
    private BigDecimal fxGainBase;
}
