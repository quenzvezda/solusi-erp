package com.solusi.erp.accountspayable.debitmemo.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DebitMemoSummaryResponse extends BaseAuditResponse {
    private String code;
    private LocalDate memoDate;
    private Long vendorId;
    private String vendorName;
    private String vendorCode;
    private Long currencyId;
    private String currencyCode;
    private Long purchaseReturnId;
    private String purchaseReturnCode;
    private BigDecimal grossAmountOriginal;
    private BigDecimal settledAmount;
    private BigDecimal refundedAmount;
    private BigDecimal remainingAmount;
    private String settlementStatus;
}
