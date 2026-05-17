package com.solusi.erp.accountspayable.vendorpayment.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VendorPaymentLineRequest {
    private Long id;

    @NotNull
    private Long vendorBillId;

    private String billCode;

    @NotNull
    private BigDecimal outstandingAmount;

    @NotNull
    private BigDecimal paidAmount;
}
