package com.solusi.erp.accountspayable.debitmemoallocation.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class DebitMemoAllocationSummaryResponse {
    private Long id;
    private String code;
    private Long debitMemoId;
    private String debitMemoCode;
    private Long vendorId;
    private String vendorName;
    private String vendorCode;
    private Long currencyId;
    private String currencyCode;
    private LocalDate allocationDate;
    private String status;
    private BigDecimal totalAppliedGrossOriginal;
    private BigDecimal totalApReductionBase;
    private BigDecimal totalFxLossBase;
    private BigDecimal totalFxGainBase;
}
