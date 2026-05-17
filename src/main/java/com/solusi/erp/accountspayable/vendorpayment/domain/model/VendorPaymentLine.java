package com.solusi.erp.accountspayable.vendorpayment.domain.model;

import java.math.BigDecimal;

public final class VendorPaymentLine {

    private final Long id;
    private final Long vendorBillId;
    private final String billCode;
    private final BigDecimal outstandingAmount;
    private final BigDecimal paidAmount;

    public VendorPaymentLine(Long id, Long vendorBillId, String billCode,
                             BigDecimal outstandingAmount, BigDecimal paidAmount) {
        this.id = id;
        this.vendorBillId = vendorBillId;
        this.billCode = billCode;
        this.outstandingAmount = outstandingAmount;
        this.paidAmount = paidAmount;
    }

    public Long getId() { return id; }
    public Long getVendorBillId() { return vendorBillId; }
    public String getBillCode() { return billCode; }
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
}
