package com.solusi.erp.inventory.goodsreceipt.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GoodsReceiptSummaryResponse extends BaseAuditResponse {
    private String code;
    private LocalDate receiptDate;
    private String referenceType;
    private String referenceCode;
    private String supplierName;
    private String status;
    private int lineCount;
    private BigDecimal totalAmount;
}
