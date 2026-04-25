package com.solusi.erp.purchasing.purchaseorder.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderSummaryResponse extends BaseAuditResponse {
    private String code;
    private LocalDate orderDate;
    private Long supplierId;
    private String supplierName;
    private PurchaseOrderType poType;
    private PurchaseOrderStatus status;
    private BigDecimal totalAmount;
    private int lineCount;
}
