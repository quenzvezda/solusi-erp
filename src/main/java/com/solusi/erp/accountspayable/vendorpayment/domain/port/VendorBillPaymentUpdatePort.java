package com.solusi.erp.accountspayable.vendorpayment.domain.port;

import java.util.List;

public interface VendorBillPaymentUpdatePort {
    void updatePaymentStatus(List<Long> vendorBillIds);
}
