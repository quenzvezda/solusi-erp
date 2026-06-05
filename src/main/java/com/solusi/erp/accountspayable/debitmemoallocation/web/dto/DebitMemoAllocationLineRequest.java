package com.solusi.erp.accountspayable.debitmemoallocation.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DebitMemoAllocationLineRequest {
    private Long id;

    @NotNull
    private Long vendorBillId;

    private String vendorBillCode;
    private BigDecimal debitMemoRemainingAtDraft;
    private BigDecimal vendorBillOutstandingAtDraft;

    @NotNull
    private BigDecimal appliedGrossOriginal;
}
