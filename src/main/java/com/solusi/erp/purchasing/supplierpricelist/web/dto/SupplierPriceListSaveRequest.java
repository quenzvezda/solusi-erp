package com.solusi.erp.purchasing.supplierpricelist.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SupplierPriceListSaveRequest extends BaseAuditResponse {

    @Size(max = 30, message = "{label.spl.code} {validation.size.suffix}")
    private String code;

    @NotNull(message = "{label.spl.supplier} {validation.notblank.suffix}")
    private Long supplierId;

    @NotNull(message = "{label.spl.product} {validation.notblank.suffix}")
    private Long productId;

    @NotNull(message = "{label.spl.uom} {validation.notblank.suffix}")
    private Long uomId;

    @NotNull(message = "{label.spl.currency} {validation.notblank.suffix}")
    private Long currencyId;

    @NotNull(message = "{label.spl.unitPrice} {validation.notblank.suffix}")
    @DecimalMin(value = "0.0001", message = "{msg.error.spl.price.positive}")
    private BigDecimal unitPrice;

    @NotNull(message = "{label.spl.minQuantity} {validation.notblank.suffix}")
    private BigDecimal minQuantity;

    @NotNull(message = "{label.spl.effectiveFrom} {validation.notblank.suffix}")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private String note;

    private boolean active = true;

    private String supplierName;
    private String productName;
    private String uomName;
    private String currencyName;
}
