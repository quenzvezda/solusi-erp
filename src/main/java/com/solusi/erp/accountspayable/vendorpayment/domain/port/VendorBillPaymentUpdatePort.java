package com.solusi.erp.accountspayable.vendorpayment.domain.port;

import com.solusi.erp.accountspayable.vendorpayment.domain.model.VendorPayment;

import java.util.List;

public interface VendorBillPaymentUpdatePort {
    void lockAndValidatePayment(VendorPayment payment);

    void updateSettlementStatus(List<Long> vendorBillIds);
}
