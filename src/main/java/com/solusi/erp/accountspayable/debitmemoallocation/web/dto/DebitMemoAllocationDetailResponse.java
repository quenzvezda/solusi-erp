package com.solusi.erp.accountspayable.debitmemoallocation.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class DebitMemoAllocationDetailResponse {
    private Long id;
    private String code;
    private Long debitMemoId;
    private String debitMemoCode;
    private LocalDate allocationDate;
    private String status;
    private BigDecimal totalAppliedGrossOriginal;
    private BigDecimal totalDppOriginal;
    private BigDecimal totalTaxOriginal;
    private BigDecimal totalGrirReversalBase;
    private BigDecimal totalTaxReversalBase;
    private BigDecimal totalApReductionBase;
    private BigDecimal totalFxLossBase;
    private BigDecimal totalFxGainBase;
    private Long applyJournalEntryId;
    private Long reversalJournalEntryId;
    private LocalDate reversalDate;
    private String reversalReason;
    private String notes;
    private List<DebitMemoAllocationLineResponse> lines;
}
