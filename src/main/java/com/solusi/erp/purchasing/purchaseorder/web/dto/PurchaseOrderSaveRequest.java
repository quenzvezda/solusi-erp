package com.solusi.erp.purchasing.purchaseorder.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderSaveRequest extends BaseAuditResponse {
    private String code;
    @NotNull(message = "{label.po.orderDate} {validation.notnull.suffix}")
    private LocalDate orderDate;
    private LocalDate expectedDate;
    @NotNull(message = "{label.po.supplier} {validation.notnull.suffix}")
    private Long supplierId;
    private Long facilityId;
    @NotNull(message = "{label.po.currency} {validation.notnull.suffix}")
    private Long currencyId;
    @NotNull(message = "{label.po.exchangeRate} {validation.notnull.suffix}")
    private BigDecimal exchangeRate;
    private int paymentTermDays = 30;
    private Long prId;
    private PurchaseOrderType poType = PurchaseOrderType.DIRECT;
    @NotNull(message = "{label.po.tax} {validation.notnull.suffix}")
    private Long taxId;
    private String taxCode;
    private String taxName;
    private BigDecimal taxRate = BigDecimal.ZERO;
    private TaxCalculationMode taxCalculationMode = TaxCalculationMode.EXCLUSIVE;
    private String note;
    private PurchaseOrderStatus status;
    private Long approverId;
    @Valid
    private List<PurchaseOrderLineRequest> lines = new ArrayList<>();
}
