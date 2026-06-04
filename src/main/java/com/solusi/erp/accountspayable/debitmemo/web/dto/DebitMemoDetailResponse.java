package com.solusi.erp.accountspayable.debitmemo.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DebitMemoDetailResponse extends BaseAuditResponse {
    private String code;
    private Long purchaseReturnId;
    private String purchaseReturnCode;
    private Long generatedGoodsIssueId;
    private Long vendorId;
    private Long currencyId;
    private LocalDate memoDate;
    private BigDecimal grossAmountOriginal;
    private BigDecimal dppAmountOriginal;
    private BigDecimal taxAmountOriginal;
    private BigDecimal grossAmountBase;
    private BigDecimal dppAmountBase;
    private BigDecimal taxAmountBase;
    private BigDecimal settledAmount;
    private BigDecimal refundedAmount;
    private BigDecimal remainingAmount;
    private String settlementStatus;
    private String supplierMemoNumber;
    private LocalDate supplierMemoDate;
    private String taxDocumentNumber;
    private LocalDate taxDocumentDate;
    private String notes;
    private List<DebitMemoLineResponse> lines;
}
