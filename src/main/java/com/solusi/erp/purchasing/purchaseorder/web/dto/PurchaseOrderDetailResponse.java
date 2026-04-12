package com.solusi.erp.purchasing.purchaseorder.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderDetailResponse extends BaseAuditResponse {
    private String code;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private Long supplierId;
    private String supplierName;
    private Long facilityId;
    private String facilityName;
    private Long currencyId;
    private String currencyName;
    private BigDecimal exchangeRate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private PurchaseOrderStatus status;
    private int paymentTermDays;
    private Long prId;
    private String note;
    private List<PurchaseOrderLineResponse> lines;
}
