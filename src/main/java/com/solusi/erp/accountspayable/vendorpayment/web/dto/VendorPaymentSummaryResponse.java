package com.solusi.erp.accountspayable.vendorpayment.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class VendorPaymentSummaryResponse {
    private Long id;
    private String code;
    private Long vendorId;
    private String vendorName;
    private LocalDate paymentDate;
    private BigDecimal paymentAmount;
    private String bankAccountName;
    private String status;
}
