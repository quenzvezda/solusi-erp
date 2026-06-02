package com.solusi.erp.purchasing.purchasereturn.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseReturnSaveRequest extends BaseAuditResponse {

    @NotNull(message = "{label.purchase-return.return-date} {validation.notnull.suffix}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
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
    @NotNull(message = "{label.purchase-return.reason} {validation.notnull.suffix}")
    private PurchaseReturnReason reason;
    private String note;
    @Valid
    private List<PurchaseReturnSaveLineRequest> lines = new ArrayList<>();
}
