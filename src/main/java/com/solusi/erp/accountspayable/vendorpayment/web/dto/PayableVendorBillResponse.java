package com.solusi.erp.accountspayable.vendorpayment.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class PayableVendorBillResponse {
    private Long vendorBillId;
    private String billCode;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
}
