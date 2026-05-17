package com.solusi.erp.accountspayable.vendorpayment.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class VendorPaymentDetailResponse {
    private Long id;
    private String code;
    private Long vendorId;
    private Long currencyId;
    private Long bankAccountId;
    private LocalDate paymentDate;
    private BigDecimal exchangeRate;
    private BigDecimal paymentAmount;
    private String status;
    private String reference;
    private String notes;
    private List<VendorPaymentLineResponse> lines;
}
