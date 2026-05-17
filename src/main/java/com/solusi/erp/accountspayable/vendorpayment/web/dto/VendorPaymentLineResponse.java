package com.solusi.erp.accountspayable.vendorpayment.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class VendorPaymentLineResponse {
    private Long id;
    private Long vendorBillId;
    private String billCode;
    private BigDecimal outstandingAmount;
    private BigDecimal paidAmount;
}
