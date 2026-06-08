package com.solusi.erp.purchasing.purchasereturn.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseReturnDetailResponse extends BaseAuditResponse {

    private String code;
    private LocalDate returnDate;
    private Long goodsReceiptId;
    private String goodsReceiptCode;
    private Long purchaseOrderId;
    private String purchaseOrderCode;
    private Long supplierId;
    private String supplierName;
    private Long facilityId;
    private String facilityName;
    private Long currencyId;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private PurchaseReturnStatus status;
    private PurchaseReturnReason reason;
    private String note;
    private Long submittedByUserId;
    private Long generatedGoodsIssueId;
    private LocalDate reversalDate;
    private String reversalReason;
    private Long reversedByUserId;
    private Long reversalJournalEntryId;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
    private List<PurchaseReturnLineDetailResponse> lines;
}
