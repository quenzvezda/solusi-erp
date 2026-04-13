package com.solusi.erp.purchasing.purchaserequisition.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PurchaseRequisitionLineRequest {

    private Long id;
    private Integer version;

    @NotNull(message = "{label.product} {validation.notnull.suffix}")
    private Long productId;

    private String productName;

    @NotNull(message = "{label.qty} {validation.notnull.suffix}")
    @DecimalMin(value = "0.0001", message = "{msg.error.pr.line.quantity.positive}")
    private BigDecimal quantity;

    @NotNull(message = "{label.uom} {validation.notnull.suffix}")
    private Long uomId;

    private String uomName;

    private LocalDate requiredDate;

    private BigDecimal estimatedUnitPrice;

    private String note;
}
