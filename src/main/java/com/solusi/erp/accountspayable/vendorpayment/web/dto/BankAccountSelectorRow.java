package com.solusi.erp.accountspayable.vendorpayment.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountSelectorRow {
    private Long id;
    private String accountName;
    private String accountNumber;
    private String bankName;
    private String paymentType;
    private String currencyCode;
    private String coaCode;
}
