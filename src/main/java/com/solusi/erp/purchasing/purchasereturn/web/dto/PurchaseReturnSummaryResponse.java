package com.solusi.erp.purchasing.purchasereturn.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseReturnSummaryResponse extends BaseAuditResponse {

    private String code;
    private LocalDate returnDate;
    private String supplierName;
    private Long goodsReceiptId;
    private String goodsReceiptCode;
    private Long purchaseOrderId;
    private String purchaseOrderCode;
    private PurchaseReturnStatus status;
    private BigDecimal totalQuantity;
    private BigDecimal totalAmount;
}
